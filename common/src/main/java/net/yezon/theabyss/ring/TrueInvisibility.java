package net.yezon.theabyss.ring;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.living.LivingChangeTargetEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingIncomingDamageEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerEvent;
import net.yezon.theabyss.platform.event.tick.ServerTickEvent;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.TrueInvisibilityMessage;
import net.yezon.theabyss.mixin.accessor.MobSelectorsAccessor;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class TrueInvisibility {
    private record State(long until, float radius) {
    }

    private static final Map<UUID, State> ACTIVE = new HashMap<>();
    private static final Set<UUID> BREAKS_ON_ATTACK = new HashSet<>();
    private static final double MOB_RANGE = 64.0;

    private TrueInvisibility() {
    }

    public static void start(ServerPlayer player, int ticks, float radius) {
        BREAKS_ON_ATTACK.remove(player.getUUID());
        begin(player, ticks, radius);
        shatter(player.serverLevel(), player);
    }

    public static void startStep(ServerPlayer player, int ticks) {
        BREAKS_ON_ATTACK.add(player.getUUID());
        begin(player, ticks, 0.0F);
    }

    private static void begin(ServerPlayer player, int ticks, float radius) {
        ServerLevel level = player.serverLevel();
        ACTIVE.put(player.getUUID(), new State(level.getGameTime() + ticks, radius));
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new TrueInvisibilityMessage(player.getId(), ticks, radius));
        loseHunters(player, radius);
    }

    public static boolean hiddenAt(Player player, double distance) {
        State state = ACTIVE.get(player.getUUID());
        return state != null && distance > state.radius;
    }

    public static void loseHunters(ServerPlayer player, float radius) {
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(MOB_RANGE), m -> hunts(m, player))) {
            if (mob.distanceTo(player) > radius) {
                mob.setTarget(null);
                ((MobSelectorsAccessor)mob).theabyss$targetSelector().getAvailableGoals().forEach(WrappedGoal::stop);
                mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            }
        }
    }

    private static boolean hunts(Mob mob, Player player) {
        return mob.getTarget() == player
            || mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) == player;
    }

    private static void shatter(ServerLevel level, Player player) {
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};
        double[] heights = {1.75, 1.25, 0.75, 0.15, 1.0, 1.0};
        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = player.getItemBySlot(slots[i]);
            double y = player.getY() + heights[i];
            if (!stack.isEmpty()) {
                level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(stack.getItem())), player.getX(), y, player.getZ(), 14, 0.25, 0.12, 0.25, 0.08);
            }
        }
        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 1.0, player.getZ(), 10, 0.3, 0.5, 0.3, 0.02);
    }

    private static void end(ServerPlayer player) {
        ACTIVE.remove(player.getUUID());
        BREAKS_ON_ATTACK.remove(player.getUUID());
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new TrueInvisibilityMessage(player.getId(), 0, 0.0F));
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer watcher) {
            State state = ACTIVE.get(target.getUUID());
            if (state != null) {
                long left = state.until - target.level().getGameTime();
                if (left > 0) {
                    PacketDistributor.sendToPlayer(watcher, new TrueInvisibilityMessage(target.getId(), (int)left, state.radius));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() instanceof ServerPlayer player && hiddenAt(player, event.getEntity().distanceTo(player))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onDealDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer attacker && BREAKS_ON_ATTACK.contains(attacker.getUUID())) {
            attacker.removeEffect(MobEffects.INVISIBILITY);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, State>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, State> entry = it.next();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                BREAKS_ON_ATTACK.remove(entry.getKey());
                it.remove();
                continue;
            }
            if (!player.hasEffect(MobEffects.INVISIBILITY) || player.level().getGameTime() >= entry.getValue().until) {
                BREAKS_ON_ATTACK.remove(entry.getKey());
                it.remove();
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new TrueInvisibilityMessage(player.getId(), 0, 0.0F));
                continue;
            }
            if (player.tickCount % 5 == 0) {
                loseHunters(player, entry.getValue().radius);
            }
        }
    }
}
