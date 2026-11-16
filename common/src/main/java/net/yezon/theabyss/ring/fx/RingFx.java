package net.yezon.theabyss.ring.fx;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.network.RingFxMessage;
import net.yezon.theabyss.platform.services.ServerHooks;

public final class RingFx {
    public static final int WAVE = 1;
    public static final int DOME = 2;
    public static final int PILLAR = 3;
    public static final int BOLT = 4;
    public static final int IMPLODE = 5;
    public static final int VORTEX = 6;
    public static final int LENS = 8;
    private static final double SHADER_RANGE = 128.0;

    private RingFx() {
    }

    public static void shader(ServerLevel level, int kind, Vec3 at, float radius, int ticks, int color, float param) {
        RingFxMessage message = new RingFxMessage(kind, at.x, at.y, at.z, radius, ticks, color, param);
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceToSqr(at) <= SHADER_RANGE * SHADER_RANGE && player.connection != null && ServerHooks.get().hasChannel(player.connection, RingFxMessage.TYPE)) {
                PacketDistributor.sendToPlayer(player, message);
            }
        }
    }

    public static void wave(ServerLevel level, Vec3 at, float radius, int ticks, int color) {
        shader(level, WAVE, at, radius, ticks, color, 1.2F);
    }

    public static void dome(ServerLevel level, Vec3 at, float radius, int ticks, int color) {
        shader(level, DOME, at, radius, ticks, color, 0.0F);
    }

    public static void pillar(ServerLevel level, Vec3 at, float height, int ticks, int color, float width) {
        shader(level, PILLAR, at, height, ticks, color, width);
    }

    public static void bolt(ServerLevel level, Vec3 at, int ticks, int color) {
        shader(level, BOLT, at, 90.0F, ticks, color, 0.22F);
    }

    public static void implode(ServerLevel level, Vec3 at, float radius, int ticks, int color) {
        shader(level, IMPLODE, at, radius, ticks, color, 0.8F);
    }

    public static void vortex(ServerLevel level, Vec3 at, float radius, int ticks, int color, int arms) {
        shader(level, VORTEX, at, radius, ticks, color, arms);
    }

    public static void burst(ServerLevel level, net.minecraft.core.particles.ParticleOptions glow, Vec3 at, int count, double spread, double speed) {
        level.sendParticles(glow, at.x, at.y, at.z, count, spread, spread, spread, speed);
    }

    public static void emit(ServerLevel level, net.minecraft.core.particles.ParticleOptions glow, Vec3 at, Vec3 velocity) {
        double speed = velocity.length();
        if (speed < 1.0E-6) {
            level.sendParticles(glow, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
            return;
        }
        Vec3 d = velocity.scale(1.0 / speed);
        level.sendParticles(glow, at.x, at.y, at.z, 0, d.x, d.y, d.z, speed);
    }

    public static void ring(ServerLevel level, net.minecraft.core.particles.ParticleOptions glow, Vec3 centre, double radius, int count, double speed, double rise) {
        for (int i = 0; i < count; i++) {
            double a = i * Math.PI * 2.0 / count;
            Vec3 out = new Vec3(Math.cos(a), 0.0, Math.sin(a));
            emit(level, glow, centre.add(out.scale(radius)), out.scale(speed).add(0.0, rise, 0.0));
        }
    }

    public static void spiral(ServerLevel level, net.minecraft.core.particles.ParticleOptions glow, Vec3 centre, double radius, double height, double turns, int count, double rise) {
        for (int i = 0; i < count; i++) {
            double f = i / (double)count;
            double a = f * turns * Math.PI * 2.0;
            Vec3 at = centre.add(Math.cos(a) * radius, f * height, Math.sin(a) * radius);
            Vec3 tangent = new Vec3(-Math.sin(a), 0.0, Math.cos(a)).scale(0.04);
            emit(level, glow, at, tangent.add(0.0, rise, 0.0));
        }
    }

    public static void line(ServerLevel level, net.minecraft.core.particles.ParticleOptions glow, Vec3 a, Vec3 b, double step) {
        double length = a.distanceTo(b);
        int n = Math.max(1, (int)Math.ceil(length / step));
        for (int i = 0; i <= n; i++) {
            emit(level, glow, a.lerp(b, i / (double)n), Vec3.ZERO);
        }
    }

    public static void arc(ServerLevel level, net.minecraft.core.particles.ParticleOptions glow, Vec3 a, Vec3 b, double jag, net.minecraft.util.RandomSource random) {
        int segments = Mth.clamp((int)(a.distanceTo(b) / 0.8), 3, 24);
        Vec3 previous = a;
        Vec3 axis = b.subtract(a);
        Vec3 side1 = axis.cross(new Vec3(0.0, 1.0, 0.0));
        if (side1.lengthSqr() < 1.0E-4) {
            side1 = new Vec3(1.0, 0.0, 0.0);
        }
        side1 = side1.normalize();
        Vec3 side2 = axis.cross(side1).normalize();
        for (int i = 1; i <= segments; i++) {
            double f = i / (double)segments;
            double envelope = Math.sin(f * Math.PI);
            Vec3 point = i == segments ? b : a.add(axis.scale(f))
                .add(side1.scale((random.nextDouble() - 0.5) * 2.0 * jag * envelope))
                .add(side2.scale((random.nextDouble() - 0.5) * 2.0 * jag * envelope));
            line(level, glow, previous, point, 0.18);
            previous = point;
        }
    }
}
