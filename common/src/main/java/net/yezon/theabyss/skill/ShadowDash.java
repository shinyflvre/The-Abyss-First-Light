package net.yezon.theabyss.skill;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.living.LivingFallEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerEvent;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.TeleportDashMessage;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.fx.FxOptions;
import net.yezon.theabyss.ring.fx.RingFx;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class ShadowDash {
    public static final int COOLDOWN = 160;
    private static final int NO_FALL_TICKS = 60;
    private static final double RANGE = 5.0;
    private static final int STEPS = 20;
    private static final int RUSH_TICKS = 6;
    private static final int SHADOW = 0x0D0A12;
    private static final int SHADOW_SOFT = 0x231D2C;
    private static final Map<UUID, Long> READY = new HashMap<>();
    private static final Map<UUID, Long> NO_FALL_UNTIL = new HashMap<>();

    private ShadowDash() {
    }

    public static void dash(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator() || !Skills.has(player, Skill.SHADOW_DASH)) {
            return;
        }
        ServerLevel level = player.level();
        long now = level.getGameTime();
        Long ready = READY.get(player.getUUID());
        if (ready != null && now < ready) {
            return;
        }
        Vec3 dir = player.getLookAngle().normalize();
        Vec3 from = player.position();
        Vec3 dest = target(level, player, dir);
        if (dest.distanceToSqr(from) < 1.0) {
            return;
        }
        READY.put(player.getUUID(), now + COOLDOWN);
        if (player.isPassenger()) {
            player.stopRiding();
        }
        Vec3 fromEye = player.getEyePosition();
        depart(level, player, dir);
        PacketDistributor.sendToPlayer(player, new TeleportDashMessage(fromEye.x, fromEye.y, fromEye.z, player.getYRot(), player.getXRot()));
        Vec3 motion = player.getDeltaMovement();
        player.teleportTo(dest.x, dest.y, dest.z);
        player.connection.teleport(dest.x, dest.y, dest.z, player.getYRot(), player.getXRot());
        player.setDeltaMovement(motion.x, Math.max(0.0, motion.y), motion.z);
        player.resetFallDistance();
        NO_FALL_UNTIL.put(player.getUUID(), now + NO_FALL_TICKS);
        Skills.sound(level, player.blockPosition(), "theabyss:spell_of_teleport", 0.7F, 1.35F);
        Skills.sound(level, player.blockPosition(), "minecraft:particle.soul_escape", 1.4F, 0.55F);
        Vec3 to = dest;
        for (int tick = 0; tick < RUSH_TICKS; tick++) {
            float a = tick / (float)RUSH_TICKS;
            float b = (tick + 1) / (float)RUSH_TICKS;
            TheabyssMod.queueServerWork(tick + 1, () -> trail(level, from.lerp(to, rush(a)), from.lerp(to, rush(b))));
        }
        TheabyssMod.queueServerWork(RUSH_TICKS, () -> arrive(level, to));
    }

    private static Vec3 target(ServerLevel level, ServerPlayer player, Vec3 dir) {
        AABB box = player.getBoundingBox();
        Vec3 start = player.position();
        Vec3 best = start;
        double lift = 0.0;
        for (int i = 1; i <= STEPS; i++) {
            Vec3 offset = dir.scale(RANGE * i / STEPS);
            double found = -1.0;
            for (double up = lift; up <= lift + 1.13; up += 0.125) {
                if (level.noCollision(player, box.move(offset.x, offset.y + up, offset.z))) {
                    found = up;
                    break;
                }
            }
            if (found < 0.0) {
                break;
            }
            lift = found;
            best = start.add(offset.x, offset.y + lift, offset.z);
        }
        return best;
    }

    private static float rush(float t) {
        float k = 1.0F - t;
        return 1.0F - k * k * k;
    }

    private static void depart(ServerLevel level, ServerPlayer player, Vec3 dir) {
        RandomSource random = player.getRandom();
        FxOptions dark = RingVisuals.smoke(SHADOW, 0.5F, 30).gravity(-0.001F).drag(0.87F);
        FxOptions soft = RingVisuals.smoke(SHADOW_SOFT, 0.38F, 24).gravity(-0.003F).drag(0.87F);
        for (int i = 0; i < 24; i++) {
            Vec3 at = new Vec3(player.getRandomX(0.45), player.getRandomY(), player.getRandomZ(0.45));
            Vec3 motion = dir.scale(0.04 + random.nextDouble() * 0.16).add(0.0, 0.005, 0.0);
            RingFx.emit(level, i % 3 == 0 ? soft : dark, at, motion);
        }
        level.sendParticles(ParticleTypes.SQUID_INK, player.getX(), player.getY() + 0.9, player.getZ(), 10, 0.25, 0.45, 0.25, 0.03);
        RingFx.ring(level, RingVisuals.smoke(SHADOW, 0.4F, 26).drag(0.85F), player.position().add(0.0, 0.1, 0.0), 0.3, 12, 0.14, 0.0);
    }

    private static void trail(ServerLevel level, Vec3 a, Vec3 b) {
        FxOptions streak = RingVisuals.smoke(SHADOW, 0.3F, 20).gravity(-0.001F).drag(0.84F);
        FxOptions wisp = RingVisuals.smoke(SHADOW_SOFT, 0.22F, 16).gravity(-0.003F).drag(0.84F);
        RandomSource random = level.getRandom();
        int n = Math.max(3, (int)(a.distanceTo(b) * 3.0));
        for (int i = 0; i < n; i++) {
            Vec3 p = a.lerp(b, (i + random.nextDouble()) / n).add((random.nextDouble() - 0.5) * 0.35, 0.1 + random.nextDouble() * 1.5,
                (random.nextDouble() - 0.5) * 0.35);
            RingFx.emit(level, i % 2 == 0 ? streak : wisp, p, new Vec3((random.nextDouble() - 0.5) * 0.03, 0.005, (random.nextDouble() - 0.5) * 0.03));
        }
    }

    private static void arrive(ServerLevel level, Vec3 feet) {
        RingFx.ring(level, RingVisuals.smoke(SHADOW, 0.42F, 28).drag(0.86F), feet.add(0.0, 0.12, 0.0), 0.35, 14, 0.16, 0.0);
        RingFx.burst(level, RingVisuals.smoke(SHADOW_SOFT, 0.34F, 22).gravity(-0.003F), feet.add(0.0, 0.9, 0.0), 6, 0.3, 0.015);
        level.sendParticles(ParticleTypes.SQUID_INK, feet.x, feet.y + 0.6, feet.z, 6, 0.3, 0.3, 0.3, 0.02);
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        Long until = NO_FALL_UNTIL.get(event.getEntity().getUUID());
        if (until == null) {
            return;
        }
        if (event.getEntity().level().getGameTime() <= until) {
            event.setCanceled(true);
        } else {
            NO_FALL_UNTIL.remove(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        READY.remove(event.getEntity().getUUID());
        NO_FALL_UNTIL.remove(event.getEntity().getUUID());
    }
}
