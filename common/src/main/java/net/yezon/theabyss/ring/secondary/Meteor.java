package net.yezon.theabyss.ring.secondary;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.NightbladeImpactMessage;
import net.yezon.theabyss.network.NightbladeShakeMessage;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.fx.RingFx;
import net.yezon.theabyss.util.FallGuard;
import net.yezon.theabyss.util.RingMode;

public final class Meteor {
    public static final double RANGE = 40.0;
    public static final int IMPACT = 30;
    public static final int FALL_START = 8;
    public static final double RADIUS = 5.0;
    public static final double SKY = 34.0;

    private Meteor() {
    }

    public static double damage(double upgrade) {
        return net.yezon.theabyss.ring.RingDamage.METEOR.at(upgrade);
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.level();
        Vec3 spot = Aim.spot(level, player, RANGE);
        Vec3 ahead = player.getLookAngle().multiply(1.0, 0.0, 1.0);
        ahead = ahead.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : ahead.normalize();
        Vec3 side = new Vec3(-ahead.z, 0.0, ahead.x).scale(player.getRandom().nextBoolean() ? 1.0 : -1.0);
        Vec3 sky = spot.add(ahead.scale(18.0)).add(side.scale(7.0)).add(0.0, SKY, 0.0);
        SpellFxMessage.around(level, spot, SpellFxMessage.METEOR, List.of(player.getId()),
            SpellFxMessage.points(sky, spot), IMPACT);
        RingVisuals.sound(level, player.position(), SoundEvents.BLAZE_SHOOT, 1.2F, 0.55F);
        RingVisuals.sound(level, spot, SoundEvents.FIRECHARGE_USE, 1.6F, 0.5F);
        float dmg = (float)damage(vars.ManaDamage);
        TheabyssMod.queueServerWork(FALL_START, () -> {
            RingVisuals.sound(level, spot, SoundEvents.ENDER_DRAGON_GROWL, 0.6F, 1.8F);
            RingVisuals.sound(level, sky.lerp(spot, 0.5), SoundEvents.FIREWORK_ROCKET_LAUNCH, 3.0F, 0.5F);
        });
        TheabyssMod.queueServerWork(IMPACT, () -> impact(level, player, spot, dmg));
        return true;
    }

    private static void impact(ServerLevel level, ServerPlayer caster, Vec3 at, float damage) {
        AABB box = new AABB(at, at).inflate(RADIUS, 3.0, RADIUS);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster && !RingMode.spares(caster, e))) {
            double d = victim.position().distanceTo(at);
            if (d > RADIUS) {
                continue;
            }
            float hit = Math.max(1.0F, Math.round(damage * (1.0 - 0.5 * d / RADIUS)));
            victim.damageCooldownTime = 0;
            victim.hurtServer(level, level.damageSources().indirectMagic(caster, caster), hit);
            victim.igniteForSeconds(5.0F);
            Vec3 out = victim.position().subtract(at).multiply(1.0, 0.0, 1.0);
            out = out.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 0.0) : out.normalize().scale(1.1 * (1.0 - d / RADIUS) + 0.3);
            victim.setDeltaMovement(out.x, 0.75, out.z);
            victim.needsSync = true;
            victim.syncVelocity = true;
            FallGuard.protect(victim, 60);
        }
        RingFx.wave(level, at.add(0.0, 0.1, 0.0), 11.0F, 22, RingVisuals.FIRE);
        RingFx.shader(level, RingFx.WAVE, at.add(0.0, 0.1, 0.0), 6.0F, 14, RingVisuals.EMBER, 1.6F);
        NightbladeShakeMessage.around(level, at, 32.0, 1.6F, 0.7F);
        if (caster.distanceToSqr(at) < 40.0 * 40.0) {
            NightbladeImpactMessage.send(caster, false);
        }
        BlockState ground = level.getBlockState(BlockPos.containing(at.x, at.y - 0.5, at.z));
        if (!ground.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), at.x, at.y + 0.3, at.z, 90, 1.6, 0.3, 1.6, 0.45);
        }
        level.sendParticles(ParticleTypes.LAVA, at.x, at.y + 0.4, at.z, 26, 1.4, 0.3, 1.4, 0.0);
        RingVisuals.sound(level, at, SoundEvents.GENERIC_EXPLODE.value(), 3.0F, 0.6F);
        RingVisuals.sound(level, at, SoundEvents.DRAGON_FIREBALL_EXPLODE, 2.0F, 0.7F);
        RingVisuals.sound(level, at, SoundEvents.MACE_SMASH_GROUND_HEAVY, 2.0F, 0.6F);
        RingVisuals.sound(level, at, SoundEvents.LAVA_POP, 1.5F, 0.6F);
    }
}
