package net.yezon.theabyss.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.init.TheabyssModSounds;
import net.yezon.theabyss.ring.fx.FxOptions;
import net.yezon.theabyss.ring.fx.RingFx;

final class AbyssaurFx {
    static final int CYAN = 0x14E2FF;
    static final int AQUA = 0x79FFEB;
    static final int SKY = 0x7BD4FF;
    static final int BILE_DARK = 0x0B5A6E;

    private AbyssaurFx() {
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

    static SoundEvent roarSound() {
        return TheabyssModSounds.ABYSSAUR_ATTACK.get();
    }

    private static void dust(ServerLevel level, Vec3 at, int count, double spread) {
        BlockState ground = level.getBlockState(BlockPos.containing(at.x, at.y - 0.5, at.z));
        if (!ground.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), at.x, at.y + 0.1, at.z, count, spread, 0.1, spread, 0.2);
        }
    }

    static void bite(ServerLevel level, Vec3 jaws, boolean hit) {
        level.playSound(null, jaws.x, jaws.y, jaws.z, SoundEvents.EVOKER_FANGS_ATTACK, SoundSource.HOSTILE, 1.4F, 0.6F);
        if (hit) {
            level.playSound(null, jaws.x, jaws.y, jaws.z, SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 1.0F, 0.6F);
            RingFx.burst(level, sparkle(CYAN, 0.24F, 12), jaws, 14, 0.35, 0.16);
            RingFx.emit(level, orb(AQUA, 1.0F, 5), jaws, Vec3.ZERO);
        }
    }

    static void lungeJump(ServerLevel level, LivingEntity dino) {
        level.playSound(null, dino.getX(), dino.getY(), dino.getZ(), roarSound(), SoundSource.HOSTILE, 1.2F, 1.3F);
        dust(level, dino.position(), 24, 0.8);
        ring(level, dino.position(), CYAN, 2.2F, 10);
    }

    static void lungeTrail(ServerLevel level, LivingEntity dino) {
        Vec3 at = dino.position().add(0.0, 1.2, 0.0);
        RingFx.emit(level, wisp(CYAN, 0.3F, 10).gravity(-0.005F), at, Vec3.ZERO);
        dust(level, dino.position(), 4, 0.5);
    }

    static void tailWhoosh(ServerLevel level, LivingEntity dino) {
        level.playSound(null, dino.getX(), dino.getY(), dino.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.6F, 0.4F);
    }

    static void tailArc(ServerLevel level, Vec3 centre, Vec3 forward, double radius) {
        RandomSource random = level.getRandom();
        for (int i = 0; i < 28; i++) {
            double a = Math.toRadians(70 + i * (220.0 / 27.0));
            Vec3 dir = forward.yRot((float)a);
            Vec3 at = centre.add(dir.scale(radius * (0.6 + random.nextDouble() * 0.4))).add(0.0, 0.4 + random.nextDouble() * 0.6, 0.0);
            Vec3 tangent = new Vec3(-dir.z, 0.0, dir.x).scale(0.18);
            RingFx.emit(level, (i % 3 == 0 ? zap(AQUA, 0.26F, 8) : sparkle(CYAN, 0.24F, 10)).drag(0.85F), at, tangent);
        }
        dust(level, centre, 20, 2.0);
    }

    static void roar(ServerLevel level, LivingEntity dino, double radius) {
        Vec3 jaws = dino.position().add(0.0, 2.6, 0.0);
        level.playSound(null, jaws.x, jaws.y, jaws.z, roarSound(), SoundSource.HOSTILE, 3.0F, 0.8F);
        level.playSound(null, jaws.x, jaws.y, jaws.z, SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.4F, 0.5F);
        RingFx.wave(level, dino.position(), (float)radius, 26, CYAN);
        ring(level, dino.position(), CYAN, (float)radius * 0.35F, 16);
        ring(level, dino.position(), AQUA, (float)radius * 0.7F, 22);
        ring(level, dino.position(), SKY, (float)radius, 28);
        for (int i = 0; i < 30; i++) {
            double a = i * Math.PI * 2.0 / 30.0;
            RingFx.emit(level, sparkle(i % 2 == 0 ? CYAN : AQUA, 0.28F, 20).drag(0.92F), jaws, new Vec3(Math.cos(a) * 0.55, 0.1, Math.sin(a) * 0.55));
        }
    }

    static void dread(ServerLevel level, LivingEntity victim) {
        RingFx.burst(level, smoke(BILE_DARK, 0.9F, 24), victim.position().add(0.0, victim.getBbHeight() * 0.6, 0.0), 6, 0.4, 0.03);
    }

    static void breath(ServerLevel level, Vec3 jaws, Vec3 dir, double range, double halfAngle) {
        RandomSource random = level.getRandom();
        for (int i = 0; i < 10; i++) {
            Vec3 d = dir.yRot((float)((random.nextDouble() * 2 - 1) * halfAngle)).add(0.0, -0.12 + random.nextDouble() * 0.12, 0.0).normalize();
            double speed = 0.45 + random.nextDouble() * 0.35;
            FxOptions bile = (i % 3 == 0 ? smoke(BILE_DARK, 0.8F, 18) : i % 3 == 1 ? wisp(CYAN, 0.36F, 14) : sparkle(AQUA, 0.26F, 14)).drag(0.88F).gravity(0.01F);
            RingFx.emit(level, bile, jaws, d.scale(speed * range / 8.0));
        }
        if (random.nextInt(3) == 0) {
            level.playSound(null, jaws.x, jaws.y, jaws.z, SoundEvents.BREEZE_SHOOT, SoundSource.HOSTILE, 0.8F, 0.5F);
        }
    }

    static void breathStart(ServerLevel level, Vec3 jaws) {
        level.playSound(null, jaws.x, jaws.y, jaws.z, roarSound(), SoundSource.HOSTILE, 1.4F, 1.4F);
        level.playSound(null, jaws.x, jaws.y, jaws.z, SoundEvents.BREWING_STAND_BREW, SoundSource.HOSTILE, 1.0F, 0.5F);
    }

    static void pool(ServerLevel level, Vec3 at, double radius, int tick) {
        if (tick % 4 != 0) {
            return;
        }
        RandomSource random = level.getRandom();
        for (int i = 0; i < 3; i++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double r = Math.sqrt(random.nextDouble()) * radius;
            Vec3 p = at.add(Math.cos(a) * r, 0.08, Math.sin(a) * r);
            RingFx.emit(level, (i == 0 ? sparkle(AQUA, 0.2F, 14) : orb(CYAN, 0.24F, 10)).gravity(-0.01F), p, new Vec3(0.0, 0.02, 0.0));
        }
        if (tick % 20 == 0) {
            ring(level, at, BILE_DARK, (float)radius, 22);
        }
    }

    static void stagger(ServerLevel level, LivingEntity dino) {
        RingFx.burst(level, sparkle(SKY, 0.24F, 16), dino.position().add(0.0, 2.0, 0.0), 16, 0.6, 0.1);
    }
}
