package net.yezon.theabyss.ring.secondary;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModSounds;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.util.RingMode;

public final class MeteorStrike {
    public static final int SHOTS = 8;
    public static final int GAP = 3;
    public static final double SPEED = 2.6;
    public static final double RANGE = 48.0;
    public static final double SPLASH = 1.2;

    private MeteorStrike() {
    }

    public static double damage(double upgrade) {
        return net.yezon.theabyss.ring.RingDamage.METEOR_STRIKE.at(upgrade);
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.level();
        float damage = (float)damage(vars.ManaDamage);
        for (int i = 0; i < SHOTS; i++) {
            int shot = i;
            TheabyssMod.queueServerWork(i * GAP, () -> {
                if (player.isRemoved() || !player.isAlive() || player.level() != level) {
                    return;
                }
                fire(level, player, damage, shot);
            });
        }
        return true;
    }

    private static void fire(ServerLevel level, ServerPlayer player, float damage, int shot) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double spread = 0.012 * (shot % 3 - 1);
        Vec3 dir = look.add(-look.z * spread, (shot % 2 == 0 ? 0.008 : -0.008), look.x * spread).normalize();
        Vec3 from = RingVisuals.hand(player).add(dir.scale(0.8));
        Vec3 end = eye.add(dir.scale(RANGE));
        BlockHitResult wall = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 to = wall.getType() == HitResult.Type.BLOCK ? wall.getLocation() : end;
        AABB box = new AABB(eye, to).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, to, box,
            e -> e instanceof LivingEntity && e.isAlive() && e.isPickable() && e != player && !RingMode.spares(player, e), eye.distanceToSqr(to));
        LivingEntity target = null;
        if (hit != null && hit.getEntity() instanceof LivingEntity living) {
            target = living;
            to = hit.getLocation();
        }
        boolean block = target == null && wall.getType() == HitResult.Type.BLOCK;
        int flight = Math.max(1, (int)Math.ceil(from.distanceTo(to) / SPEED));
        SpellFxMessage.around(level, from, to, new SpellFxMessage(SpellFxMessage.METEOR_BOLT, List.of(player.getId()),
            SpellFxMessage.points(from, to), flight));
        RingVisuals.sound(level, from, TheabyssModSounds.SPELL_OF_BLACKSTRIKE_SHOOT.get(), 0.7F, 1.25F + player.getRandom().nextFloat() * 0.2F);
        RingVisuals.sound(level, from, SoundEvents.FIRECHARGE_USE, 0.35F, 1.4F + player.getRandom().nextFloat() * 0.2F);
        LivingEntity aimed = target;
        Vec3 point = to;
        Vec3 face = wall.getType() == HitResult.Type.BLOCK ? wall.getDirection().getUnitVec3() : Vec3.ZERO;
        TheabyssMod.queueServerWork(flight, () -> impact(level, player, aimed, point, face, block, damage, dir));
    }

    private static void impact(ServerLevel level, ServerPlayer player, LivingEntity aimed, Vec3 at, Vec3 face, boolean block, float damage, Vec3 dir) {
        boolean owner = !player.isRemoved() && player.level() == level;
        net.minecraft.world.damagesource.DamageSource source = owner ? level.damageSources().indirectMagic(player, player) : level.damageSources().magic();
        List<LivingEntity> victims = new java.util.ArrayList<>();
        if (aimed != null && aimed.isAlive() && aimed.level() == level && aimed.position().distanceTo(at) < 4.0) {
            victims.add(aimed);
        }
        for (Entity e : level.getEntities((Entity)null, new AABB(at, at).inflate(SPLASH), e -> e instanceof LivingEntity && e.isAlive() && e != player)) {
            LivingEntity living = (LivingEntity)e;
            if (!victims.contains(living) && !(owner && RingMode.spares(player, living))) {
                victims.add(living);
            }
        }
        for (LivingEntity victim : victims) {
            if (!victim.isAlive()) {
                continue;
            }
            victim.damageCooldownTime = 0;
            victim.hurtServer(level, source, damage);
            Vec3 push = dir.multiply(1.0, 0.0, 1.0);
            push = push.lengthSqr() < 1.0E-4 ? Vec3.ZERO : push.normalize().scale(0.25);
            victim.push(push.x, 0.08, push.z);
            victim.needsSync = true;
        }
        if (block) {
            BlockState state = level.getBlockState(BlockPos.containing(at.subtract(face.scale(0.1))));
            if (!state.isAir()) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), at.x, at.y, at.z, 10, 0.2, 0.2, 0.2, 0.15);
            }
        }
        RingVisuals.sound(level, at, TheabyssModSounds.SPELL_OF_BLACKSTRIKE_HIT.get(), 0.6F, 1.3F + level.getRandom().nextFloat() * 0.2F);
        RingVisuals.sound(level, at, SoundEvents.AMETHYST_BLOCK_HIT, 0.8F, 0.7F + level.getRandom().nextFloat() * 0.2F);
    }
}
