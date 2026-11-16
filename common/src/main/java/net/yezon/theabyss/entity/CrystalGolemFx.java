package net.yezon.theabyss.entity;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.ring.fx.FxOptions;
import net.yezon.theabyss.ring.fx.RingFx;

final class CrystalGolemFx {
    static final int PINK = 0xFF6FE0;
    static final int LILAC = 0xB27CFF;
    static final int PALE = 0xFFD2F6;

    private CrystalGolemFx() {
    }

    private static FxOptions sparkle(int color, float size, int life) {
        return TheabyssModParticleTypes.FX_SPARKLE.get().of(color, size, life, FxOptions.FLICKER);
    }

    private static FxOptions zap(int color, float size, int life) {
        return TheabyssModParticleTypes.FX_ZAP.get().of(color, size, life, FxOptions.FLICKER | FxOptions.HOT);
    }

    private static FxOptions orb(int color, float size, int life) {
        return TheabyssModParticleTypes.FX_ORB.get().of(color, size, life, FxOptions.SHRINK);
    }

    private static FxOptions wisp(int color, float size, int life) {
        return TheabyssModParticleTypes.FX_WISP.get().of(color, size, life, FxOptions.ANIMATE);
    }

    private static FxOptions smoke(int color, float size, int life) {
        return TheabyssModParticleTypes.FX_SMOKE.get().of(color, size, life, FxOptions.SMOKE | FxOptions.GROW);
    }

    private static void ring(ServerLevel level, Vec3 centre, int color, float radius, int life) {
        RingFx.emit(level, TheabyssModParticleTypes.FX_RING.get().of(color, radius, life, FxOptions.FLAT | FxOptions.GROW), centre.add(0.0, 0.06, 0.0), Vec3.ZERO);
    }

    private static void runes(ServerLevel level, Vec3 centre, int color, double radius, int count, double spin, float size, int life) {
        for (int i = 0; i < count; i++) {
            double a = i * Math.PI * 2.0 / count;
            Vec3 at = centre.add(Math.cos(a) * radius, 0.1, Math.sin(a) * radius);
            Vec3 tangent = new Vec3(-Math.sin(a), 0.0, Math.cos(a)).scale(spin);
            RingFx.emit(level, sparkle(color, size, life).drag(0.9F), at, tangent);
        }
    }

    static void ironCharge(ServerLevel level, CrystalGolemEntity golem) {
        level.playSound(null, golem.getX(), golem.getY() + 1.5, golem.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 1.6F, 0.6F);
        level.playSound(null, golem.getX(), golem.getY() + 1.5, golem.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 0.8F, 1.6F);
    }

    static void ironHit(ServerLevel level, Vec3 fist, Vec3 forward) {
        level.playSound(null, fist.x, fist.y, fist.z, SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE, 1.4F, 0.55F);
        level.playSound(null, fist.x, fist.y, fist.z, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.HOSTILE, 1.4F, 0.7F);
        level.playSound(null, fist.x, fist.y, fist.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 0.5F, 1.4F);
        RandomSource random = level.getRandom();
        for (int i = 0; i < 26; i++) {
            Vec3 v = forward.scale(0.25 + random.nextDouble() * 0.45).add((random.nextDouble() - 0.5) * 0.5, (random.nextDouble() - 0.3) * 0.4, (random.nextDouble() - 0.5) * 0.5);
            RingFx.emit(level, (i % 3 == 0 ? zap(PINK, 0.32F, 9) : sparkle(i % 2 == 0 ? PINK : PALE, 0.24F, 14)).drag(0.82F), fist, v);
        }
        RingFx.emit(level, orb(PINK, 1.6F, 6), fist, Vec3.ZERO);
        RingFx.emit(level, orb(PALE, 0.9F, 4), fist, Vec3.ZERO);
    }

    static void ironMiss(ServerLevel level, Vec3 fist) {
        level.playSound(null, fist.x, fist.y, fist.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.2F, 0.5F);
        RingFx.burst(level, sparkle(PINK, 0.2F, 10), fist, 10, 0.3, 0.12);
    }

    static void ironParried(ServerLevel level, Vec3 fist) {
        level.playSound(null, fist.x, fist.y, fist.z, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE, 1.5F, 0.8F);
        RingFx.burst(level, sparkle(PALE, 0.26F, 16), fist, 30, 0.5, 0.25);
    }

    static void punchHit(ServerLevel level, Vec3 fist) {
        RingFx.burst(level, sparkle(PINK, 0.2F, 10), fist, 8, 0.3, 0.15);
    }

    static void slamRaise(ServerLevel level, CrystalGolemEntity golem) {
        level.playSound(null, golem.getX(), golem.getY() + 2.0, golem.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 1.4F, 0.5F);
    }

    static void slamTelegraph(ServerLevel level, Vec3 centre, float radius) {
        RingFx.implode(level, centre, radius, 14, LILAC);
        ring(level, centre, LILAC, radius, 16);
        ring(level, centre, PINK, radius * 0.45F, 16);
        runes(level, centre, LILAC, radius * 0.72, 18, 0.05, 0.22F, 16);
        level.playSound(null, centre.x, centre.y, centre.z, SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 0.9F, 1.8F);
    }

    static void slamImpact(ServerLevel level, Vec3 centre, float radius) {
        level.playSound(null, centre.x, centre.y, centre.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.0F, 0.6F);
        level.playSound(null, centre.x, centre.y, centre.z, SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.0F, 0.5F);
        level.playSound(null, centre.x, centre.y, centre.z, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.HOSTILE, 1.8F, 0.6F);
        RingFx.wave(level, centre, radius, 20, PINK);
        ring(level, centre, PINK, radius, 20);
        ring(level, centre, LILAC, radius * 0.72F, 24);
        ring(level, centre, PALE, radius * 0.38F, 28);
        runes(level, centre, PINK, radius * 0.55, 24, 0.08, 0.26F, 22);
        runes(level, centre, LILAC, radius * 0.86, 32, -0.06, 0.22F, 22);
        RingFx.emit(level, orb(PINK, 2.4F, 7), centre.add(0.0, 0.3, 0.0), Vec3.ZERO);
        RandomSource random = level.getRandom();
        for (int i = 0; i < 30; i++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double s = 0.18 + random.nextDouble() * 0.3;
            RingFx.emit(level, zap(i % 2 == 0 ? PINK : LILAC, 0.3F, 12).gravity(0.04F).flags(FxOptions.FLICKER | FxOptions.HOT | FxOptions.BOUNCE),
                centre.add(0.0, 0.2, 0.0), new Vec3(Math.cos(a) * s, 0.25 + random.nextDouble() * 0.35, Math.sin(a) * s));
        }
        BlockState ground = level.getBlockState(net.minecraft.core.BlockPos.containing(centre.x, centre.y - 0.5, centre.z));
        if (!ground.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), centre.x, centre.y + 0.1, centre.z, 60, 1.4, 0.1, 1.4, 0.25);
        }
        for (int i = 0; i < 10; i++) {
            double a = i * Math.PI * 2.0 / 10.0;
            RingFx.emit(level, smoke(0x5A4A6A, 1.4F, 30), centre.add(Math.cos(a) * 1.2, 0.2, Math.sin(a) * 1.2), new Vec3(Math.cos(a) * 0.12, 0.03, Math.sin(a) * 0.12));
        }
    }

    static void waveFront(ServerLevel level, Vec3 centre, float radius) {
        int count = Math.max(10, (int)(radius * 5.0F));
        RandomSource random = level.getRandom();
        for (int i = 0; i < count; i++) {
            double a = (i + random.nextDouble()) * Math.PI * 2.0 / count;
            Vec3 at = centre.add(Math.cos(a) * radius, 0.15, Math.sin(a) * radius);
            RingFx.emit(level, (i % 3 == 0 ? wisp(LILAC, 0.32F, 10) : sparkle(PINK, 0.24F, 10)).gravity(-0.01F), at,
                new Vec3(Math.cos(a) * 0.05, 0.06 + random.nextDouble() * 0.05, Math.sin(a) * 0.05));
        }
    }

    static void eruptionStomp(ServerLevel level, Vec3 foot) {
        level.playSound(null, foot.x, foot.y, foot.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 0.6F, 0.5F);
        level.playSound(null, foot.x, foot.y, foot.z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 1.6F, 0.7F);
        ring(level, foot, LILAC, 2.4F, 14);
        ring(level, foot, PINK, 1.3F, 12);
        BlockState ground = level.getBlockState(net.minecraft.core.BlockPos.containing(foot.x, foot.y - 0.5, foot.z));
        if (!ground.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), foot.x, foot.y + 0.1, foot.z, 30, 0.8, 0.1, 0.8, 0.2);
        }
    }

    static void throwGrab(ServerLevel level, Vec3 shoulder) {
        level.playSound(null, shoulder.x, shoulder.y, shoulder.z, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE, 1.4F, 0.6F);
        RingFx.burst(level, sparkle(PINK, 0.22F, 12), shoulder, 14, 0.4, 0.12);
    }

    static void throwRelease(ServerLevel level, Vec3 hand) {
        level.playSound(null, hand.x, hand.y, hand.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.2F, 0.5F);
    }

    static void shardShatter(ServerLevel level, Vec3 at) {
        level.playSound(null, at.x, at.y, at.z, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.HOSTILE, 2.0F, 0.6F);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.0F, 0.6F);
        ring(level, at, PINK, 2.6F, 12);
        RandomSource random = level.getRandom();
        for (int i = 0; i < 22; i++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double s = 0.12 + random.nextDouble() * 0.25;
            RingFx.emit(level, zap(i % 2 == 0 ? PINK : LILAC, 0.26F, 12).gravity(0.04F).flags(FxOptions.FLICKER | FxOptions.HOT | FxOptions.BOUNCE),
                at.add(0.0, 0.3, 0.0), new Vec3(Math.cos(a) * s, 0.2 + random.nextDouble() * 0.3, Math.sin(a) * s));
        }
        RingFx.emit(level, orb(PINK, 1.8F, 6), at.add(0.0, 0.4, 0.0), Vec3.ZERO);
    }

    static void chargeWindup(ServerLevel level, Vec3 feet, Vec3 dir, double length) {
        level.playSound(null, feet.x, feet.y, feet.z, SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.0F, 0.6F);
        for (double d = 1.5; d < length; d += 0.7) {
            RingFx.emit(level, sparkle(d % 1.4 < 0.7 ? LILAC : PINK, 0.2F, 26).drag(0.8F), feet.add(dir.scale(d)).add(0.0, 0.1, 0.0), new Vec3(0.0, 0.01, 0.0));
        }
    }

    static void chargeStep(ServerLevel level, CrystalGolemEntity golem) {
        Vec3 feet = golem.position();
        BlockState ground = level.getBlockState(golem.blockPosition().below());
        if (!ground.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), feet.x, feet.y + 0.1, feet.z, 6, 0.5, 0.05, 0.5, 0.1);
        }
        RingFx.emit(level, smoke(0x5A4A6A, 1.1F, 18), feet.add(0.0, 0.3, 0.0), new Vec3(0.0, 0.03, 0.0));
    }

    static void chargeHit(ServerLevel level, LivingEntity victim) {
        Vec3 at = victim.position().add(0.0, victim.getBbHeight() * 0.5, 0.0);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE, 1.4F, 0.5F);
        RingFx.burst(level, sparkle(PINK, 0.26F, 12), at, 16, 0.5, 0.2);
    }

    static void chargeWall(ServerLevel level, Vec3 at) {
        level.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 0.8F, 0.5F);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.0F, 0.4F);
        RingFx.burst(level, sparkle(PALE, 0.28F, 30), at, 30, 0.9, 0.18);
        RingFx.burst(level, sparkle(LILAC, 0.2F, 40).gravity(-0.005F), at.add(0.0, 1.0, 0.0), 12, 0.6, 0.04);
    }

    static void dazedTick(ServerLevel level, CrystalGolemEntity golem, int tick) {
        if (tick % 3 != 0) {
            return;
        }
        double a = tick * 0.35;
        Vec3 head = golem.position().add(Math.cos(a) * 0.9, 3.5, Math.sin(a) * 0.9);
        RingFx.emit(level, sparkle(tick % 6 == 0 ? PALE : PINK, 0.22F, 8), head, Vec3.ZERO);
    }

    static void waveHit(ServerLevel level, LivingEntity victim) {
        RingFx.burst(level, sparkle(PINK, 0.24F, 12), victim.position().add(0.0, victim.getBbHeight() * 0.5, 0.0), 12, 0.4, 0.15);
    }
}
