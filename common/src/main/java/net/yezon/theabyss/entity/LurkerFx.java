package net.yezon.theabyss.entity;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.ring.fx.FxOptions;
import net.yezon.theabyss.ring.fx.RingFx;

final class LurkerFx {
    static final int SOBBER = 0x38C5DE;
    static final int SOBBER_DEEP = 0x1F91AA;
    static final int GLOW = 0x80FFDB;
    static final int NIGHT = 0x0A2942;

    private LurkerFx() {
    }

    private static FxOptions sparkle(int color, float size, int life) {
        return TheabyssModParticleTypes.FX_SPARKLE.get().of(color, size, life, FxOptions.FLICKER);
    }

    private static FxOptions orb(int color, float size, int life) {
        return TheabyssModParticleTypes.FX_ORB.get().of(color, size, life, FxOptions.SHRINK);
    }

    private static FxOptions drop(int color, float size, int life) {
        return TheabyssModParticleTypes.FX_ORB.get().of(color, size, life, FxOptions.SHRINK | FxOptions.BOUNCE).gravity(0.04F);
    }

    private static FxOptions smoke(int color, float size, int life) {
        return TheabyssModParticleTypes.FX_SMOKE.get().of(color, size, life, FxOptions.SMOKE | FxOptions.GROW);
    }

    private static FxOptions ring(int color, float radius, int life) {
        return TheabyssModParticleTypes.FX_RING.get().of(color, radius, life, FxOptions.FLAT | FxOptions.GROW);
    }

    static void sound(ServerLevel level, Vec3 at, String id, float volume, float pitch) {
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(id));
        if (sound != null) {
            level.playSound(null, at.x, at.y, at.z, sound, SoundSource.HOSTILE, volume, pitch);
        }
    }

    static void bite(ServerLevel level, Vec3 jaws, boolean hit) {
        sound(level, jaws, "minecraft:entity.evoker_fangs.attack", 0.7F, 1.5F);
        if (hit) {
            sound(level, jaws, "minecraft:entity.player.attack.strong", 0.8F, 0.7F);
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, jaws.x, jaws.y, jaws.z, 3, 0.2, 0.2, 0.2, 0.1);
        }
        RandomSource random = level.getRandom();
        for (int i = 0; i < 6; i++) {
            Vec3 v = new Vec3(random.nextDouble() - 0.5, random.nextDouble() * 0.4, random.nextDouble() - 0.5).scale(0.12);
            RingFx.emit(level, drop(i % 2 == 0 ? SOBBER : SOBBER_DEEP, 0.07F, 16), jaws, v);
        }
    }

    static void spitCharge(ServerLevel level, Vec3 throat) {
        sound(level, throat, "minecraft:block.honey_block.slide", 1.0F, 0.6F);
        sound(level, throat, "minecraft:entity.llama.angry", 0.7F, 0.55F);
        for (int i = 0; i < 6; i++) {
            RingFx.emit(level, orb(GLOW, 0.1F, 12), throat.add((level.getRandom().nextDouble() - 0.5) * 0.4, -0.2, (level.getRandom().nextDouble() - 0.5) * 0.4),
                new Vec3(0.0, 0.02, 0.0));
        }
    }

    static void spit(ServerLevel level, Vec3 jaws, Vec3 dir) {
        sound(level, jaws, "minecraft:entity.llama.spit", 1.2F, 0.55F);
        sound(level, jaws, "minecraft:block.slime_block.fall", 0.9F, 0.8F);
        RandomSource random = level.getRandom();
        for (int i = 0; i < 10; i++) {
            Vec3 v = dir.scale(0.15 + random.nextDouble() * 0.2).add((random.nextDouble() - 0.5) * 0.12, random.nextDouble() * 0.1, (random.nextDouble() - 0.5) * 0.12);
            RingFx.emit(level, drop(i % 3 == 0 ? GLOW : SOBBER, 0.08F, 18), jaws, v);
        }
        RingFx.emit(level, smoke(SOBBER_DEEP, 0.35F, 14).drag(0.85F), jaws, dir.scale(0.08));
    }

    static void splash(ServerLevel level, Vec3 at, boolean onEntity) {
        sound(level, at, "minecraft:block.slime_block.break", 1.1F, 0.6F);
        sound(level, at, "minecraft:block.honey_block.break", 0.9F, 0.7F);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, TheabyssModItems.LURKER_SOBBER.get()), at.x, at.y + 0.2, at.z, 18, 0.25, 0.2, 0.25, 0.12);
        RandomSource random = level.getRandom();
        for (int i = 0; i < 16; i++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double s = 0.12 + random.nextDouble() * 0.18;
            RingFx.emit(level, drop(i % 3 == 0 ? GLOW : i % 3 == 1 ? SOBBER : SOBBER_DEEP, 0.09F, 22), at.add(0.0, 0.25, 0.0),
                new Vec3(Math.cos(a) * s, 0.12 + random.nextDouble() * 0.2, Math.sin(a) * s));
        }
        if (!onEntity) {
            RingFx.emit(level, ring(SOBBER, 1.6F, 14), at.add(0.0, 0.05, 0.0), Vec3.ZERO);
        }
    }

    static void puddle(ServerLevel level, Vec3 at, double radius, int tick) {
        RandomSource random = level.getRandom();
        if (tick % 3 == 0) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double r = Math.sqrt(random.nextDouble()) * radius;
            RingFx.emit(level, orb(random.nextInt(3) == 0 ? GLOW : SOBBER, 0.07F + random.nextFloat() * 0.05F, 16),
                at.add(Math.cos(a) * r, 0.06, Math.sin(a) * r), new Vec3(0.0, 0.012, 0.0));
        }
        if (tick % 12 == 0) {
            RingFx.emit(level, ring(SOBBER_DEEP, (float)radius * 1.7F, 16), at.add(0.0, 0.03, 0.0), Vec3.ZERO);
        }
    }

    static void stuck(ServerLevel level, LivingEntity victim) {
        if (level.getRandom().nextInt(3) == 0) {
            RingFx.emit(level, drop(SOBBER, 0.06F, 14), new Vec3(victim.getRandomX(0.4), victim.getY() + 0.3, victim.getRandomZ(0.4)), new Vec3(0.0, -0.02, 0.0));
        }
    }

    static void fade(ServerLevel level, LivingEntity lurker, boolean in) {
        sound(level, lurker.position(), "minecraft:block.sculk.spread", 0.8F, in ? 0.6F : 1.1F);
        sound(level, lurker.position(), in ? "minecraft:entity.phantom.flap" : "minecraft:entity.ravager.roar", in ? 0.6F : 0.55F, in ? 0.5F : 1.7F);
        RandomSource random = level.getRandom();
        for (int i = 0; i < (in ? 14 : 22); i++) {
            Vec3 at = new Vec3(lurker.getRandomX(0.7), lurker.getY() + random.nextDouble() * lurker.getBbHeight(), lurker.getRandomZ(0.7));
            Vec3 v = in ? new Vec3(0.0, 0.015, 0.0) : at.subtract(lurker.position().add(0.0, 1.0, 0.0)).normalize().scale(0.12);
            RingFx.emit(level, i % 2 == 0 ? sparkle(GLOW, 0.1F, 14) : smoke(NIGHT, 0.35F, 18).drag(0.86F), at, v);
        }
        if (!in) {
            RingFx.emit(level, ring(GLOW, 2.4F, 12), lurker.position().add(0.0, 0.05, 0.0), Vec3.ZERO);
        }
    }

    static void leap(ServerLevel level, LivingEntity lurker) {
        sound(level, lurker.position(), "minecraft:entity.goat.long_jump", 1.0F, 0.6F);
        level.sendParticles(ParticleTypes.POOF, lurker.getX(), lurker.getY() + 0.1, lurker.getZ(), 8, 0.4, 0.05, 0.4, 0.02);
    }
}
