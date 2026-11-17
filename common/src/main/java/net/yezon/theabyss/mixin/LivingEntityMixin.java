package net.yezon.theabyss.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.yezon.theabyss.mixin.accessor.WalkAnimationStateAccessor;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.gravity.Gravity;
import net.yezon.theabyss.gravity.GravityEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    @Final
    public WalkAnimationState walkAnimation;

    @Unique
    private boolean theabyss$travelFramed;
    @Unique
    private boolean theabyss$jumpFramed;

    @Shadow
    protected abstract void updateWalkAnimation(float distance);

    @Inject(method = "travel", at = @At("HEAD"))
    private void theabyss$gravityTravelIn(Vec3 input, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        GravityEntity gravity = (GravityEntity)self;
        gravity.theabyss$setLastTravel(self.tickCount);
        if (gravity.theabyss$gravityId() == 0 || gravity.theabyss$framed()) {
            return;
        }
        gravity.theabyss$setFramed(true);
        this.theabyss$travelFramed = true;
        self.setDeltaMovement(Gravity.toLocal(self.getDeltaMovement(), Gravity.of(self)));
    }

    @Inject(method = "travel", at = @At("RETURN"))
    private void theabyss$gravityTravelOut(Vec3 input, CallbackInfo ci) {
        if (!this.theabyss$travelFramed) {
            return;
        }
        this.theabyss$travelFramed = false;
        LivingEntity self = (LivingEntity)(Object)this;
        ((GravityEntity)self).theabyss$setFramed(false);
        self.setDeltaMovement(Gravity.toWorld(self.getDeltaMovement(), Gravity.of(self)));
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"))
    private void theabyss$gravityJumpIn(CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        GravityEntity gravity = (GravityEntity)self;
        if (gravity.theabyss$gravityId() == 0 || gravity.theabyss$framed()) {
            return;
        }
        this.theabyss$jumpFramed = true;
        self.setDeltaMovement(Gravity.toLocal(self.getDeltaMovement(), Gravity.of(self)));
    }

    @Inject(method = "jumpFromGround", at = @At("RETURN"))
    private void theabyss$gravityJumpOut(CallbackInfo ci) {
        if (!this.theabyss$jumpFramed) {
            return;
        }
        this.theabyss$jumpFramed = false;
        LivingEntity self = (LivingEntity)(Object)this;
        self.setDeltaMovement(Gravity.toWorld(self.getDeltaMovement(), Gravity.of(self)));
    }

    @ModifyReturnValue(method = "maxUpStep", at = @At("RETURN"))
    private float theabyss$gravityStep(float original) {
        return ((GravityEntity)this).theabyss$gravityId() != 0 ? 0.0F : original;
    }

    @Inject(method = "checkFallDamage", at = @At("HEAD"), cancellable = true)
    private void theabyss$gravityFall(double ya, boolean onGround, BlockState onState, BlockPos pos, CallbackInfo ci) {
        if (((GravityEntity)this).theabyss$gravityId() != 0) {
            ((LivingEntity)(Object)this).resetFallDistance();
            ci.cancel();
        }
    }

    @WrapMethod(method = "hasLineOfSight(Lnet/minecraft/world/entity/Entity;)Z")
    private boolean theabyss$gravitySight(Entity target, Operation<Boolean> original) {
        LivingEntity self = (LivingEntity)(Object)this;
        boolean selfTurned = ((GravityEntity)self).theabyss$gravityId() != 0;
        boolean targetTurned = ((GravityEntity)target).theabyss$gravityId() != 0;
        if (!selfTurned && !targetTurned || target.level() != self.level()) {
            return original.call(target);
        }
        ClipContext.Block block = ClipContext.Block.COLLIDER;
        ClipContext.Fluid fluid = ClipContext.Fluid.NONE;
        double eyeHeight = target.getEyeY();
        Vec3 from = self.getEyePosition();
        Vec3 to = targetTurned && eyeHeight == target.getEyeY() ? target.getEyePosition() : new Vec3(target.getX(), eyeHeight, target.getZ());
        if (to.distanceTo(from) > 128.0) {
            return false;
        }
        return self.level().clip(new ClipContext(from, to, block, fluid, self)).getType() == HitResult.Type.MISS;
    }

    @Inject(method = "calculateEntityAnimation", at = @At("HEAD"), cancellable = true)
    private void theabyss$gravityWalk(boolean useY, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (((GravityEntity)self).theabyss$gravityId() == 0) {
            return;
        }
        Direction g = Gravity.of(self);
        Vec3 moved = Gravity.toLocal(new Vec3(self.getX() - self.xo, self.getY() - self.yo, self.getZ() - self.zo), g);
        if (!self.isPassenger() && self.isAlive()) {
            this.updateWalkAnimation((float)Math.sqrt(moved.x * moved.x + moved.z * moved.z));
        } else {
            WalkAnimationStateAccessor walk = (WalkAnimationStateAccessor)this.walkAnimation;
            walk.theabyss$setSpeedOld(0.0F);
            walk.theabyss$setSpeed(0.0F);
            walk.theabyss$setPosition(0.0F);
        }
        ci.cancel();
    }
}
