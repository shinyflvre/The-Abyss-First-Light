package net.yezon.theabyss.rift;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.configuration.ConfigConfiguration;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class Blessings {
    public static final String UNDYING = "undying";
    public static final String STRENGTH = "strength";
    public static final String NIGHT = "night";
    private static final String UNDYING_READY = "undyingReadyAt";
    private static final long UNDYING_COOLDOWN = 15L * 60L * 20L;
    private static final float STRENGTH_BONUS = 1.25F;
    private static final int NIGHT_MIN_DELAY = 80;
    private static final int NIGHT_MAX_DELAY = 120;
    private static final int NIGHT_FORGET = 200;
    private static final int PURGE_INTERVAL = 1200;
    private static final Map<UUID, Spot> SPOTTED = new ConcurrentHashMap<>();

    private record Spot(UUID player, long allowAt, long forgetAt) {
    }

    private Blessings() {
    }

    public static boolean has(ServerPlayer player, String blessing) {
        return ConfigConfiguration.storyEnabled() && blessing.equals(RiftStory.data(player).getStringOr("blessing", ""));
    }

    private static long now(ServerPlayer player) {
        return player.level().getServer().overworld().getGameTime();
    }

    public static boolean useUndying(ServerPlayer player) {
        if (!has(player, UNDYING) || now(player) < RiftStory.data(player).getLongOr(UNDYING_READY, 0L)) {
            return false;
        }
        RiftStory.data(player).putLong(UNDYING_READY, now(player) + UNDYING_COOLDOWN);
        return true;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRealDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RiftStory.data(player).remove(UNDYING_READY);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target instanceof Player || !(event.getSource().getEntity() instanceof ServerPlayer player) || !has(player, STRENGTH)) {
            return;
        }
        if (TheabyssMod.MODID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).getNamespace())) {
            event.setAmount(event.getAmount() * STRENGTH_BONUS);
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(event.getNewAboutToBeSetTarget() instanceof ServerPlayer player) || !has(player, NIGHT)) {
            return;
        }
        long now = now(player);
        Spot spot = SPOTTED.get(mob.getUUID());
        if (spot == null || !spot.player().equals(player.getUUID()) || now > spot.forgetAt()) {
            long delay = NIGHT_MIN_DELAY + mob.getRandom().nextInt(NIGHT_MAX_DELAY - NIGHT_MIN_DELAY + 1);
            spot = new Spot(player.getUUID(), now + delay, now + delay + NIGHT_FORGET);
            SPOTTED.put(mob.getUUID(), spot);
            net.yezon.theabyss.network.NightGraceMessage.send(player, mob.getId(), (int)delay);
        }
        if (now < spot.allowAt()) {
            event.setCanceled(true);
        } else {
            SPOTTED.put(mob.getUUID(), new Spot(spot.player(), spot.allowAt(), now + NIGHT_FORGET));
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        long now = event.getServer().overworld().getGameTime();
        if (now % PURGE_INTERVAL == 0) {
            SPOTTED.values().removeIf(spot -> now > spot.forgetAt());
        }
    }
}
