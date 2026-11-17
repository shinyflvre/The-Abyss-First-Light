package net.yezon.theabyss.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.gravity.Gravity;
import net.yezon.theabyss.gravity.GravityEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements GravityEntity {
    @Shadow
    private Vec3 position;
    @Shadow
    private EntityDimensions dimensions;
    @Shadow
    private float eyeHeight;
    @Shadow
    private boolean onGround;
    @Shadow
    public boolean horizontalCollision;
    @Shadow
    public boolean verticalCollision;
    @Shadow
    public boolean verticalCollisionBelow;
    @Shadow
    public boolean minorHorizontalCollision;
    @Shadow
    public double xo;
    @Shadow
    public double yo;
    @Shadow
    public double zo;

    @Unique
    private int theabyss$gravity;
    @Unique
    private boolean theabyss$framed;
    @Unique
    private boolean theabyss$moveFramed;
    @Unique
    private int theabyss$blocked;
    @Unique
    private int theabyss$lastTravel;
    @Unique
    private double theabyss$fromX;
    @Unique
    private double theabyss$fromY;
    @Unique
    private double theabyss$fromZ;
    @Unique
    private double theabyss$wantX;
    @Unique
    private double theabyss$wantY;
    @Unique
    private double theabyss$wantZ;
    @Unique
    private boolean theabyss$steering;
    @Unique
    private double theabyss$steerX;
    @Unique
    private double theabyss$steerY;
    @Unique
    private double theabyss$steerZ;

    @Shadow
    private static Vec3 getInputVector(Vec3 input, float speed, float yRot) {
        throw new AssertionError();
    }

    @Override
    public int theabyss$gravityId() {
        return this.theabyss$gravity;
    }

    @Override
    public void theabyss$setGravityId(int id) {
        this.theabyss$gravity = id;
    }

    @Override
    public boolean theabyss$framed() {
        return this.theabyss$framed;
    }

    @Override
    public void theabyss$setFramed(boolean framed) {
        this.theabyss$framed = framed;
    }

    @Override
    public int theabyss$blocked() {
        return this.theabyss$blocked;
    }

    @Override
    public int theabyss$lastTravel() {
        return this.theabyss$lastTravel;
    }

    @Override
    public void theabyss$setLastTravel(int tick) {
        this.theabyss$lastTravel = tick;
    }

    @Override
    public void theabyss$setSteer(double x, double y, double z, boolean on) {
        this.theabyss$steerX = x;
        this.theabyss$steerY = y;
        this.theabyss$steerZ = z;
        this.theabyss$steering = on;
    }

    @Unique
    private Direction theabyss$dir() {
        return Direction.from3DDataValue(this.theabyss$gravity);
    }

    @ModifyReturnValue(method = "makeBoundingBox()Lnet/minecraft/world/phys/AABB;", at = @At("RETURN"))
    private AABB theabyss$gravityBox(AABB original) {
        if (this.theabyss$gravity == 0 || this.dimensions == null) {
            return original;
        }
        return Gravity.box(this.position, this.theabyss$dir(), this.dimensions.width(), this.dimensions.height());
    }

    @ModifyReturnValue(method = "getEyeY", at = @At("RETURN"))
    private double theabyss$gravityEyeY(double original) {
        if (this.theabyss$gravity == 0) {
            return original;
        }
        return this.position.y - this.theabyss$dir().getStepY() * this.eyeHeight;
    }

    @ModifyReturnValue(method = "getEyePosition()Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"))
    private Vec3 theabyss$gravityEye(Vec3 original) {
        if (this.theabyss$gravity == 0) {
            return original;
        }
        Direction g = this.theabyss$dir();
        return new Vec3(this.position.x - g.getStepX() * this.eyeHeight, this.position.y - g.getStepY() * this.eyeHeight,
            this.position.z - g.getStepZ() * this.eyeHeight);
    }

    @ModifyReturnValue(method = "getEyePosition(F)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"))
    private Vec3 theabyss$gravityEyeAt(Vec3 original, float partialTick) {
        if (this.theabyss$gravity == 0) {
            return original;
        }
        Direction g = this.theabyss$dir();
        return new Vec3(
            Mth.lerp(partialTick, this.xo, this.position.x) - g.getStepX() * this.eyeHeight,
            Mth.lerp(partialTick, this.yo, this.position.y) - g.getStepY() * this.eyeHeight,
            Mth.lerp(partialTick, this.zo, this.position.z) - g.getStepZ() * this.eyeHeight
        );
    }

    @ModifyReturnValue(method = {"getViewVector", "getLookAngle", "getForward", "getUpVector"}, at = @At("RETURN"))
    private Vec3 theabyss$gravityView(Vec3 original) {
        if (this.theabyss$gravity == 0 || !((Object)this instanceof Player)) {
            return original;
        }
        return Gravity.toWorld(original, this.theabyss$dir());
    }

    @ModifyReturnValue(method = "getBlockPosBelowThatAffectsMyMovement", at = @At("RETURN"))
    private BlockPos theabyss$gravityBelow(BlockPos original) {
        if (this.theabyss$gravity == 0) {
            return original;
        }
        Direction g = this.theabyss$dir();
        return BlockPos.containing(this.position.x + g.getStepX() * 0.500001, this.position.y + g.getStepY() * 0.500001,
            this.position.z + g.getStepZ() * 0.500001);
    }

    @Inject(method = "moveRelative", at = @At("HEAD"), cancellable = true)
    private void theabyss$gravityMoveRelative(float speed, Vec3 input, CallbackInfo ci) {
        if (!this.theabyss$framed || this.theabyss$gravity == 0 || (Object)this instanceof Player) {
            return;
        }
        Entity self = (Entity)(Object)this;
        Direction g = this.theabyss$dir();
        Vec3 wanted;
        if (this.theabyss$steering) {
            double amount = Math.min(1.0, input.length()) * speed;
            wanted = new Vec3(this.theabyss$steerX * amount, this.theabyss$steerY * amount, this.theabyss$steerZ * amount);
        } else {
            wanted = Gravity.with(getInputVector(input, speed, self.getYRot()), g.getAxis(), 0.0);
        }
        Vec3 local = Gravity.toLocal(wanted, g);
        self.setDeltaMovement(self.getDeltaMovement().add(local.x, 0.0, local.z));
        ci.cancel();
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3 theabyss$gravityMoveIn(Vec3 delta) {
        Entity self = (Entity)(Object)this;
        this.theabyss$moveFramed = this.theabyss$framed && this.theabyss$gravity != 0;
        if (this.theabyss$moveFramed) {
            Direction g = this.theabyss$dir();
            delta = Gravity.toWorld(delta, g);
            self.setDeltaMovement(Gravity.toWorld(self.getDeltaMovement(), g));
            self.resetFallDistance();
        }
        this.theabyss$fromX = this.position.x;
        this.theabyss$fromY = this.position.y;
        this.theabyss$fromZ = this.position.z;
        this.theabyss$wantX = delta.x;
        this.theabyss$wantY = delta.y;
        this.theabyss$wantZ = delta.z;
        return delta;
    }

    @Inject(method = "move", at = @At("RETURN"))
    private void theabyss$gravityMoveOut(MoverType type, Vec3 delta, CallbackInfo ci) {
        double ax = this.position.x - this.theabyss$fromX;
        double ay = this.position.y - this.theabyss$fromY;
        double az = this.position.z - this.theabyss$fromZ;
        this.theabyss$blocked = theabyss$bit(this.theabyss$wantX, ax, Direction.EAST, Direction.WEST)
            | theabyss$bit(this.theabyss$wantY, ay, Direction.UP, Direction.DOWN)
            | theabyss$bit(this.theabyss$wantZ, az, Direction.SOUTH, Direction.NORTH);
        if (!this.theabyss$moveFramed) {
            return;
        }
        this.theabyss$moveFramed = false;
        Entity self = (Entity)(Object)this;
        Direction g = this.theabyss$dir();
        self.setDeltaMovement(Gravity.toLocal(self.getDeltaMovement(), g));
        Vec3 want = Gravity.toLocal(new Vec3(this.theabyss$wantX, this.theabyss$wantY, this.theabyss$wantZ), g);
        Vec3 moved = Gravity.toLocal(new Vec3(ax, ay, az), g);
        boolean vertical = Math.abs(want.y - moved.y) > 1.0E-5;
        this.verticalCollision = vertical;
        this.verticalCollisionBelow = vertical && want.y < 0.0;
        this.horizontalCollision = Math.abs(want.x - moved.x) > 1.0E-5 || Math.abs(want.z - moved.z) > 1.0E-5;
        this.minorHorizontalCollision = false;
        this.onGround = this.verticalCollisionBelow;
        self.resetFallDistance();
    }

    @Unique
    private static int theabyss$bit(double want, double moved, Direction positive, Direction negative) {
        if (want > 1.0E-7 && moved < want - 1.0E-6) {
            return 1 << positive.get3DDataValue();
        }
        if (want < -1.0E-7 && moved > want + 1.0E-6) {
            return 1 << negative.get3DDataValue();
        }
        return 0;
    }
}
