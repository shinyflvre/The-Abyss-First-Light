package net.yezon.theabyss.fluid.types;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.common.NeoForgeMod;

public final class FluidSwimming {
    private FluidSwimming() {
    }

    public static boolean swimLikeWater(LivingEntity entity, Vec3 input, double gravity) {
        boolean falling = entity.getDeltaMovement().y <= 0.0;
        double oldY = entity.getY();
        float slowDown = entity.isSprinting() ? 0.9F : 0.8F;
        float speed = 0.02F;
        float efficiency = (float)entity.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY);
        if (!entity.onGround()) {
            efficiency *= 0.5F;
        }
        if (efficiency > 0.0F) {
            slowDown += (0.54600006F - slowDown) * efficiency;
            speed += (entity.getSpeed() - speed) * efficiency;
        }
        if (entity.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            slowDown = 0.96F;
        }
        speed *= (float)entity.getAttributeValue(NeoForgeMod.SWIM_SPEED);
        entity.moveRelative(speed, input);
        entity.move(MoverType.SELF, entity.getDeltaMovement());
        Vec3 movement = entity.getDeltaMovement();
        if (entity.horizontalCollision && entity.onClimbable()) {
            movement = new Vec3(movement.x, 0.2, movement.z);
        }
        movement = movement.multiply(slowDown, 0.8F, slowDown);
        entity.setDeltaMovement(entity.getFluidFallingAdjustedMovement(gravity, falling, movement));
        Vec3 after = entity.getDeltaMovement();
        if (entity.horizontalCollision && entity.isFree(after.x, after.y + 0.6F - entity.getY() + oldY, after.z)) {
            entity.setDeltaMovement(after.x, 0.3F, after.z);
        }
        return true;
    }
}
