package net.yezon.theabyss.entity;

import net.minecraft.core.BlockPos;
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

final class RokaFx {
    static final int MINT = 0x00FFA0;
    static final int CYAN = 0x00F0FF;
    static final int PALE = 0xC2FFDD;

    private RokaFx() {
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

    private static FxOptions flame(int color, float size, int life) {
        return TheabyssModParticleTypes.FX_FLAME.get().of(color, size, life, FxOptions.ANIMATE);
    }

    private static FxOptions smoke(int color, float size, int life) {
        return TheabyssModParticleTypes.FX_SMOKE.get().of(color, size, life, FxOptions.SMOKE | FxOptions.GROW);
    }

    private static void ring(ServerLevel level, Vec3 centre, int color, float radius, int life) {
        RingFx.emit(level, TheabyssModParticleTypes.FX_RING.get().of(color, radius, life, FxOptions.FLAT | FxOptions.GROW), centre.add(0.0, 0.06, 0.0), Vec3.ZERO);
    }

    private static void dust(ServerLevel level, Vec3 at, int count, double spread) {
        BlockState ground = level.getBlockState(BlockPos.containing(at.x, at.y - 0.5, at.z));
        if (!ground.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), at.x, at.y + 0.1, at.z, count, spread, 0.1, spread, 0.2);
        }
    }

    static void hook(ServerLevel level, Vec3 fist, boolean hit) {
        level.playSound(null, fist.x, fist.y, fist.z, hit ? SoundEvents.PLAYER_ATTACK_STRONG : SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.2F, 0.6F);
        if (hit) {
            RingFx.burst(level, sparkle(MINT, 0.2F, 10), fist, 10, 0.3, 0.15);
        }
    }

    static void uppercut(ServerLevel level, Vec3 fist, boolean hit) {
        level.playSound(null, fist.x, fist.y, fist.z, SoundEvents.RAVAGER_ATTACK, SoundSource.HOSTILE, 1.2F, 0.8F);
        if (hit) {
            for (int i = 0; i < 14; i++) {
                RingFx.emit(level, (i % 2 == 0 ? zap(CYAN, 0.28F, 10) : sparkle(PALE, 0.22F, 12)).drag(0.85F), fist,
                    new Vec3((level.getRandom().nextDouble() - 0.5) * 0.3, 0.3 + level.getRandom().nextDouble() * 0.4, (level.getRandom().nextDouble() - 0.5) * 0.3));
            }
        }
    }

    static void smash(ServerLevel level, Vec3 centre, Vec3 forward, double range, double halfAngle) {
        level.playSound(null, centre.x, centre.y, centre.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 0.9F, 0.6F);
        level.playSound(null, centre.x, centre.y, centre.z, SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.8F, 0.5F);
        ring(level, centre, MINT, 1.8F, 12);
        RingFx.emit(level, orb(PALE, 1.8F, 6), centre.add(0.0, 0.3, 0.0), Vec3.ZERO);
        dust(level, centre, 40, 1.0);
        RandomSource random = level.getRandom();
        for (int ray = -4; ray <= 4; ray++) {
            Vec3 dir = forward.yRot((float)(ray / 4.0 * halfAngle));
            for (double d = 1.0; d <= range; d += 0.9) {
                Vec3 at = centre.add(dir.scale(d)).add(0.0, 0.1, 0.0);
                RingFx.emit(level, (random.nextInt(3) == 0 ? zap(CYAN, 0.26F, 10) : sparkle(MINT, 0.24F, 12)).gravity(-0.01F), at,
                    dir.scale(0.12).add(0.0, 0.05 + random.nextDouble() * 0.06, 0.0));
            }
        }
    }

    static void roar(ServerLevel level, LivingEntity roka) {
        Vec3 head = roka.position().add(0.0, roka.getBbHeight() * 0.9, 0.0);
        level.playSound(null, head.x, head.y, head.z, SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 2.2F, 0.7F);
        level.playSound(null, head.x, head.y, head.z, SoundEvents.POLAR_BEAR_WARNING, SoundSource.HOSTILE, 1.4F, 0.5F);
        RingFx.wave(level, roka.position(), 10.0F, 20, MINT);
        ring(level, roka.position(), MINT, 4.0F, 16);
        ring(level, roka.position(), CYAN, 7.0F, 20);
        ring(level, roka.position(), PALE, 10.0F, 24);
        for (int i = 0; i < 24; i++) {
            double a = i * Math.PI * 2.0 / 24.0;
            RingFx.emit(level, sparkle(i % 2 == 0 ? MINT : CYAN, 0.26F, 18).drag(0.9F), head, new Vec3(Math.cos(a) * 0.5, 0.05, Math.sin(a) * 0.5));
        }
    }

    static void rage(ServerLevel level, LivingEntity roka, int tick) {
        if (tick % 3 != 0) {
            return;
        }
        RandomSource random = level.getRandom();
        Vec3 horns = roka.position().add((random.nextDouble() - 0.5) * 0.6, roka.getBbHeight() + 0.2, (random.nextDouble() - 0.5) * 0.6);
        RingFx.emit(level, flame(random.nextBoolean() ? MINT : CYAN, 0.3F, 12).gravity(-0.02F), horns, new Vec3(0.0, 0.04, 0.0));
        Vec3 body = roka.position().add((random.nextDouble() - 0.5) * 1.2, roka.getBbHeight() * (0.3 + random.nextDouble() * 0.4), (random.nextDouble() - 0.5) * 1.2);
        RingFx.emit(level, sparkle(MINT, 0.18F, 10), body, new Vec3(0.0, 0.03, 0.0));
    }

    static void scrape(ServerLevel level, LivingEntity roka) {
        level.playSound(null, roka.getX(), roka.getY(), roka.getZ(), SoundEvents.HOGLIN_ANGRY, SoundSource.HOSTILE, 1.4F, 0.6F);
        dust(level, roka.position(), 12, 0.5);
        RingFx.emit(level, smoke(0x8FA39A, 0.9F, 16), roka.position().add(0.0, roka.getBbHeight() * 0.8, 0.0), new Vec3(0.0, 0.0, 0.0));
    }

    static void chargeStep(ServerLevel level, LivingEntity roka) {
        dust(level, roka.position(), 6, 0.4);
        Vec3 horns = roka.position().add(0.0, roka.getBbHeight() * 0.75, 0.0);
        RingFx.emit(level, sparkle(MINT, 0.2F, 10), horns, Vec3.ZERO);
    }

    static void gore(ServerLevel level, LivingEntity victim) {
        Vec3 at = victim.position().add(0.0, victim.getBbHeight() * 0.5, 0.0);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.HOGLIN_ATTACK, SoundSource.HOSTILE, 1.6F, 0.6F);
        RingFx.burst(level, zap(CYAN, 0.3F, 12), at, 14, 0.4, 0.25);
        RingFx.burst(level, sparkle(PALE, 0.22F, 14), at, 10, 0.4, 0.15);
    }

    static void wall(ServerLevel level, Vec3 at) {
        level.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 0.7F, 0.6F);
        RingFx.burst(level, sparkle(PALE, 0.26F, 24), at, 24, 0.8, 0.16);
    }

    static void leap(ServerLevel level, Vec3 from, Vec3 landing, float radius, int flight) {
        level.playSound(null, from.x, from.y, from.z, SoundEvents.RAVAGER_STEP, SoundSource.HOSTILE, 1.6F, 0.6F);
        level.playSound(null, from.x, from.y, from.z, SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 1.0F, 0.6F);
        dust(level, from, 30, 0.8);
        ring(level, from, MINT, 2.0F, 10);
        RingFx.implode(level, landing, radius, flight, CYAN);
        ring(level, landing, CYAN, radius, flight + 4);
        ring(level, landing, MINT, radius * 0.4F, flight + 4);
    }

    static void land(ServerLevel level, Vec3 at, float radius) {
        level.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.1F, 0.7F);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.0F, 0.4F);
        RingFx.wave(level, at, radius + 1.0F, 18, MINT);
        ring(level, at, PALE, radius, 14);
        ring(level, at, CYAN, radius * 0.6F, 16);
        dust(level, at, 60, 1.6);
        RandomSource random = level.getRandom();
        for (int i = 0; i < 26; i++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double s = 0.15 + random.nextDouble() * 0.3;
            RingFx.emit(level, zap(i % 2 == 0 ? MINT : CYAN, 0.28F, 12).gravity(0.04F).flags(FxOptions.FLICKER | FxOptions.HOT | FxOptions.BOUNCE),
                at.add(0.0, 0.2, 0.0), new Vec3(Math.cos(a) * s, 0.25 + random.nextDouble() * 0.3, Math.sin(a) * s));
        }
    }

    static void hitSpark(ServerLevel level, LivingEntity victim) {
        RingFx.burst(level, sparkle(MINT, 0.22F, 12), victim.position().add(0.0, victim.getBbHeight() * 0.5, 0.0), 10, 0.4, 0.15);
    }
}
