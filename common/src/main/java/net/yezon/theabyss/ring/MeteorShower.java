package net.yezon.theabyss.ring;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModSounds;
import net.yezon.theabyss.network.NightbladeShakeMessage;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.fx.RingFx;
import net.yezon.theabyss.ring.secondary.Aim;
import net.yezon.theabyss.util.FallGuard;
import net.yezon.theabyss.util.RingMode;

public final class MeteorShower {
    public static final double COST = 60.0;
    public static final int COOLDOWN = 600;
    public static final double RANGE = 40.0;
    public static final double SPREAD = 8.0;
    public static final double SKY = 24.0;
    public static final double RIFT_RADIUS = 14.0;
    public static final double SLANT = 5.0;
    public static final int OPEN = 8;
    public static final int SMALL = 6;
    public static final int FIRST = 8;
    public static final int STEP = 5;
    public static final int FALL = 10;
    public static final int BIG_LAUNCH = 40;
    public static final int BIG_FALL = 13;
    public static final int CLOSE_START = 45;
    public static final int CLOSE_END = 60;
    public static final int SHATTER = 20;
    public static final double SMALL_RADIUS = 2.5;
    public static final double BIG_RADIUS = 4.0;
    public static final int HEADER = 5;
    public static final int ROCK = 9;
    public static final int VIOLET = 0x8D45DE;
    public static final int LIGHT = 0xAD6BFF;
    public static final int SOFT = 0xC99BFF;
    public static final int PALE = 0xE3C9FF;
    public static final int DEEP = 0x6B2DB3;
    public static final int SMOKE = 0x24163A;

    private MeteorShower() {
    }

    public static int end() {
        return BIG_LAUNCH + BIG_FALL;
    }

    public static void cast(ServerLevel level, LivingEntity caster, TheabyssModVariables.PlayerVariables vars) {
        Vec3 spot = Aim.spot(level, caster, RANGE);
        Vec3 ahead = spot.subtract(caster.position()).multiply(1.0, 0.0, 1.0);
        if (ahead.lengthSqr() < 1.0) {
            ahead = caster.getLookAngle().multiply(1.0, 0.0, 1.0);
        }
        ahead = ahead.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : ahead.normalize();
        Vec3 axis = new Vec3(-ahead.z, 0.0, ahead.x);
        RandomSource random = caster.getRandom();
        double sign = random.nextBoolean() ? 1.0 : -1.0;
        Vec3 rift = spot.add(0.0, SKY, 0.0);
        Vec3 lean = axis.scale(sign * SLANT).add(ahead.scale((random.nextDouble() - 0.5) * 4.0));
        double[] angles = new double[SMALL];
        double turn = random.nextDouble() * Math.PI * 2.0;
        for (int i = 0; i < SMALL; i++) {
            angles[i] = turn + (i + 0.15 + random.nextDouble() * 0.7) * Math.PI * 2.0 / SMALL;
        }
        for (int i = SMALL - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            double t = angles[i];
            angles[i] = angles[j];
            angles[j] = t;
        }
        List<Double> data = new ArrayList<>(HEADER + ROCK * (SMALL + 1));
        data.add(rift.x);
        data.add(rift.y);
        data.add(rift.z);
        data.add(lean.x);
        data.add(lean.z);
        float small = (float)RingDamage.METEOROID.at(vars.ManaDamage);
        float big = (float)RingDamage.METEOROID_BIG.at(vars.ManaDamage);
        for (int i = 0; i < SMALL; i++) {
            double d = 2.5 + Math.sqrt(random.nextDouble()) * (SPREAD - 2.5);
            Vec3 land = Aim.ground(level, spot.add(Math.cos(angles[i]) * d, 6.0, Math.sin(angles[i]) * d), 18);
            Vec3 offset = land.subtract(spot).multiply(1.0, 0.0, 1.0).add(lean)
                .add((random.nextDouble() - 0.5) * 5.0, 0.0, (random.nextDouble() - 0.5) * 5.0);
            Vec3 from = rift.add(inside(offset));
            int launch = FIRST + i * STEP;
            rock(data, from, land, launch, FALL, false);
            schedule(level, caster, from, land, launch, FALL, false, small);
        }
        Vec3 bigFrom = rift.add(inside(lean));
        rock(data, bigFrom, spot, BIG_LAUNCH, BIG_FALL, true);
        schedule(level, caster, bigFrom, spot, BIG_LAUNCH, BIG_FALL, true, big);
        SpellFxMessage.around(level, spot, rift, new SpellFxMessage(SpellFxMessage.METEOR_SHOWER, List.of(caster.getId()), List.copyOf(data), end()));
        RingVisuals.sound(level, rift, TheabyssModSounds.SPELL_OF_BLACKHOLE_SPAWN.get(), 3.0F, 0.6F);
        RingVisuals.sound(level, spot, SoundEvents.PORTAL_TRIGGER, 0.8F, 1.6F);
        RingVisuals.sound(level, caster.position(), TheabyssModSounds.SPELL_OF_VOID_CHARGE.get(), 1.2F, 0.75F);
        TheabyssMod.queueServerWork(CLOSE_START, () -> RingVisuals.sound(level, rift, SoundEvents.BEACON_DEACTIVATE, 2.5F, 0.6F));
    }

    private static Vec3 inside(Vec3 offset) {
        double max = RIFT_RADIUS * 0.8;
        double length = offset.length();
        return length > max ? offset.scale(max / length) : offset;
    }

    private static void rock(List<Double> data, Vec3 from, Vec3 to, int launch, int fall, boolean big) {
        data.add(from.x);
        data.add(from.y);
        data.add(from.z);
        data.add(to.x);
        data.add(to.y);
        data.add(to.z);
        data.add((double)launch);
        data.add((double)fall);
        data.add(big ? 1.0 : 0.0);
    }

    private static void schedule(ServerLevel level, LivingEntity caster, Vec3 from, Vec3 to, int launch, int fall, boolean big, float damage) {
        TheabyssMod.queueServerWork(launch, () -> {
            RingVisuals.sound(level, from, SoundEvents.FIREWORK_ROCKET_LAUNCH, big ? 4.0F : 2.5F, big ? 0.4F : 0.55F + level.getRandom().nextFloat() * 0.15F);
            RingVisuals.sound(level, to, SoundEvents.FIRECHARGE_USE, big ? 1.6F : 0.9F, big ? 0.45F : 0.65F);
        });
        TheabyssMod.queueServerWork(launch + fall, () -> impact(level, caster, to, big, damage));
        TheabyssMod.queueServerWork(launch + fall + SHATTER, () -> {
            RingVisuals.sound(level, to, SoundEvents.AMETHYST_CLUSTER_BREAK, big ? 2.0F : 1.2F, big ? 0.6F : 0.8F);
            RingVisuals.sound(level, to, SoundEvents.GLASS_BREAK, big ? 1.2F : 0.6F, 0.7F);
        });
    }

    private static void impact(ServerLevel level, LivingEntity caster, Vec3 at, boolean big, float damage) {
        double radius = big ? BIG_RADIUS : SMALL_RADIUS;
        boolean owner = !caster.isRemoved() && caster.level() == level;
        DamageSource source = owner ? level.damageSources().indirectMagic(caster, caster) : level.damageSources().magic();
        AABB box = new AABB(at, at).inflate(radius, 2.5, radius);
        List<LivingEntity> hits = new ArrayList<>();
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (owner && RingMode.spares(caster, victim)) {
                continue;
            }
            if (victim.position().distanceTo(at) <= radius) {
                hits.add(victim);
            }
        }
        for (LivingEntity victim : hits) {
            if (!victim.isAlive()) {
                continue;
            }
            double d = victim.position().distanceTo(at);
            victim.damageCooldownTime = 0;
            victim.hurtServer(level, source, damage);
            Vec3 out = victim.position().subtract(at).multiply(1.0, 0.0, 1.0);
            double push = (big ? 1.0 : 0.55) * (1.0 - d / radius) + 0.25;
            out = out.lengthSqr() < 1.0E-4 ? Vec3.ZERO : out.normalize().scale(push);
            victim.setDeltaMovement(out.x, big ? 0.7 : 0.45, out.z);
            victim.needsSync = true;
            victim.syncVelocity = true;
            FallGuard.protect(victim, 60);
        }
        RingFx.shader(level, RingFx.WAVE, at.add(0.0, 0.1, 0.0), big ? 10.0F : 4.0F, big ? 20 : 12, LIGHT, 0.6F);
        BlockState ground = level.getBlockState(BlockPos.containing(at.x, at.y - 0.5, at.z));
        if (!ground.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), at.x, at.y + 0.3, at.z, big ? 90 : 32, radius * 0.35, 0.3, radius * 0.35, 0.4);
        }
        RingVisuals.sound(level, at, SoundEvents.GENERIC_EXPLODE.value(), big ? 3.0F : 1.6F, big ? 0.6F : 0.9F + level.getRandom().nextFloat() * 0.2F);
        RingVisuals.sound(level, at, SoundEvents.AMETHYST_CLUSTER_PLACE, big ? 2.0F : 1.3F, big ? 0.5F : 0.7F);
        if (big) {
            RingVisuals.sound(level, at, TheabyssModSounds.SPELL_OF_VOID_EXPLODE.get(), 2.5F, 0.8F);
            RingVisuals.sound(level, at, SoundEvents.MACE_SMASH_GROUND_HEAVY, 2.0F, 0.6F);
            NightbladeShakeMessage.around(level, at, 32.0, 1.2F, 0.6F);
        }
    }
}
