package net.yezon.theabyss.ring.secondary;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.util.RingMode;

public final class Aim {
    public static final double HELP = 0.5;
    public static final double CONE = 7.0;

    private Aim() {
    }

    public static LivingEntity creature(ServerLevel level, LivingEntity caster, double range) {
        Vec3 eye = caster.getEyePosition();
        Vec3 end = wall(level, caster, range).getLocation();
        AABB box = caster.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0);
        LivingEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (Entity e : level.getEntities(caster, box, c -> c instanceof LivingEntity && c.isAlive() && !c.isSpectator() && c.isPickable())) {
            if (RingMode.spares(caster, e)) {
                continue;
            }
            Optional<Vec3> hit = e.getBoundingBox().inflate(HELP).clip(eye, end);
            if (hit.isPresent() && hit.get().distanceToSqr(eye) < best) {
                best = hit.get().distanceToSqr(eye);
                nearest = (LivingEntity)e;
            }
        }
        return nearest != null ? nearest : cone(level, caster, range);
    }

    private static LivingEntity cone(ServerLevel level, LivingEntity caster, double range) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        double cos = Math.cos(Math.toRadians(CONE));
        LivingEntity best = null;
        double bestDot = cos;
        for (Entity e : level.getEntities(caster, new AABB(eye, eye).inflate(range), c -> c instanceof LivingEntity && c.isAlive() && !c.isSpectator()
            && c.isPickable())) {
            if (RingMode.spares(caster, e)) {
                continue;
            }
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double d = to.length();
            if (d > range || d < 1.0E-3) {
                continue;
            }
            double dot = to.scale(1.0 / d).dot(look);
            if (dot <= bestDot) {
                continue;
            }
            BlockHitResult wall = level.clip(new ClipContext(eye, e.getBoundingBox().getCenter(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
            if (wall.getType() == HitResult.Type.BLOCK) {
                continue;
            }
            bestDot = dot;
            best = (LivingEntity)e;
        }
        return best;
    }

    public static Vec3 spot(ServerLevel level, LivingEntity caster, double range) {
        LivingEntity target = creature(level, caster, range);
        if (target != null) {
            return target.position();
        }
        BlockHitResult hit = wall(level, caster, range);
        if (hit.getType() == HitResult.Type.BLOCK) {
            Vec3 at = hit.getLocation();
            Vec3 back = Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(0.5);
            return ground(level, at.add(back), 6);
        }
        return ground(level, hit.getLocation(), 24);
    }

    public static Vec3 ground(ServerLevel level, Vec3 at, int depth) {
        BlockHitResult down = level.clip(new ClipContext(at, at.subtract(0.0, depth, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
            net.minecraft.world.phys.shapes.CollisionContext.empty()));
        return down.getType() == HitResult.Type.BLOCK ? down.getLocation() : at;
    }

    private static BlockHitResult wall(ServerLevel level, LivingEntity caster, double range) {
        Vec3 eye = caster.getEyePosition();
        return level.clip(new ClipContext(eye, eye.add(caster.getLookAngle().scale(range)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
    }
}
