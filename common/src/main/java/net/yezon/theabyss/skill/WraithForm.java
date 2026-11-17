package net.yezon.theabyss.skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.living.LivingEntityUseItemEvent;
import net.yezon.theabyss.platform.event.entity.player.AttackEntityEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerInteractEvent;
import net.yezon.theabyss.platform.event.tick.ServerTickEvent;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.WraithMessage;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.TrueInvisibility;
import net.yezon.theabyss.ring.fx.FxOptions;
import net.yezon.theabyss.ring.fx.RingFx;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class WraithForm {
    public static final int DURATION = 120;
    public static final int COOLDOWN = 1200;
    private static final int SHADOW = 0x0B0910;
    private static final int SHADOW_SOFT = 0x2A2234;
    private static final Map<UUID, Long> ACTIVE = new HashMap<>();
    private static final Map<UUID, Long> READY = new HashMap<>();

    private WraithForm() {
    }

    public static boolean active(Player player) {
        return ACTIVE.containsKey(player.getUUID());
    }

    public static void toggle(ServerPlayer player) {
        if (!Skills.has(player, Skill.WRAITH_FORM)) {
            return;
        }
        if (active(player)) {
            end(player);
            return;
        }
        long now = player.level().getGameTime();
        Long ready = READY.get(player.getUUID());
        if (ready != null && now < ready) {
            player.displayClientMessage(Component.translatable("skill.theabyss.cooldown", Component.translatable("skill.theabyss.wraith_form"),
                (int)Math.ceil((ready - now) / 20.0)), true);
            return;
        }
        start(player);
    }

    private static void start(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        ACTIVE.put(player.getUUID(), level.getGameTime() + DURATION);
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, DURATION, 0, false, false, true));
        TrueInvisibility.startStep(player, DURATION);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new WraithMessage(player.getId(), DURATION));
        sink(level, player);
        Skills.sound(level, player.blockPosition(), "theabyss:night_notice", 0.9F, 0.8F);
        Skills.sound(level, player.blockPosition(), "minecraft:block.sculk.spread", 1.0F, 0.6F);
    }

    public static void end(ServerPlayer player) {
        if (ACTIVE.remove(player.getUUID()) == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        READY.put(player.getUUID(), level.getGameTime() + COOLDOWN);
        MobEffectInstance invisible = player.getEffect(MobEffects.INVISIBILITY);
        if (invisible != null && invisible.getDuration() <= DURATION) {
            player.removeEffect(MobEffects.INVISIBILITY);
        }
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new WraithMessage(player.getId(), 0));
        rise(level, player);
        Skills.sound(level, player.blockPosition(), "minecraft:particle.soul_escape", 1.6F, 0.7F);
        Skills.sound(level, player.blockPosition(), "minecraft:block.sculk.spread", 0.8F, 0.9F);
    }

    private static void sink(ServerLevel level, ServerPlayer player) {
        RandomSource random = player.getRandom();
        FxOptions dark = RingVisuals.smoke(SHADOW, 0.55F, 34).gravity(0.004F).drag(0.86F);
        FxOptions soft = RingVisuals.smoke(SHADOW_SOFT, 0.4F, 28).gravity(0.002F).drag(0.86F);
        for (int i = 0; i < 30; i++) {
            Vec3 at = new Vec3(player.getRandomX(0.5), player.getY() + random.nextDouble() * player.getBbHeight(), player.getRandomZ(0.5));
            RingFx.emit(level, i % 3 == 0 ? soft : dark, at, new Vec3((random.nextDouble() - 0.5) * 0.04, -0.05 - random.nextDouble() * 0.05,
                (random.nextDouble() - 0.5) * 0.04));
        }
        RingFx.ring(level, RingVisuals.smoke(SHADOW, 0.5F, 32).drag(0.86F), player.position().add(0.0, 0.1, 0.0), 0.4, 18, 0.2, 0.0);
        level.sendParticles(ParticleTypes.SQUID_INK, player.getX(), player.getY() + 1.0, player.getZ(), 14, 0.3, 0.55, 0.3, 0.02);
    }

    private static void rise(ServerLevel level, ServerPlayer player) {
        RandomSource random = player.getRandom();
        FxOptions dark = RingVisuals.smoke(SHADOW, 0.5F, 30).gravity(-0.006F).drag(0.88F);
        FxOptions soft = RingVisuals.smoke(SHADOW_SOFT, 0.36F, 24).gravity(-0.008F).drag(0.88F);
        for (int i = 0; i < 26; i++) {
            Vec3 at = new Vec3(player.getRandomX(0.6), player.getY() + 0.05, player.getRandomZ(0.6));
            RingFx.emit(level, i % 3 == 0 ? soft : dark, at, new Vec3((random.nextDouble() - 0.5) * 0.03, 0.05 + random.nextDouble() * 0.08,
                (random.nextDouble() - 0.5) * 0.03));
        }
        level.sendParticles(ParticleTypes.SQUID_INK, player.getX(), player.getY() + 0.4, player.getZ(), 8, 0.35, 0.2, 0.35, 0.02);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        List<ServerPlayer> ending = new ArrayList<>();
        Iterator<Map.Entry<UUID, Long>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> entry = it.next();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                it.remove();
            } else if (!player.isAlive() || !player.hasEffect(MobEffects.INVISIBILITY) || player.level().getGameTime() >= entry.getValue()) {
                ending.add(player);
            }
        }
        ending.forEach(WraithForm::end);
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && active(player)) {
            end(player);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player && active(player)) {
            end(player);
        }
    }

    @SubscribeEvent
    public static void onUseItem(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof ServerPlayer player && active(player)) {
            end(player);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer watcher) {
            Long until = ACTIVE.get(target.getUUID());
            if (until != null) {
                long left = until - target.level().getGameTime();
                if (left > 0) {
                    PacketDistributor.sendToPlayer(watcher, new WraithMessage(target.getId(), (int)left));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE.remove(event.getEntity().getUUID());
        READY.remove(event.getEntity().getUUID());
    }
}
