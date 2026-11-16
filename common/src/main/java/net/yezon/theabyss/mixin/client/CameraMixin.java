package net.yezon.theabyss.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.client.gravity.GravityClient;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    @Final
    private static Vector3fc FORWARDS;
    @Shadow
    @Final
    private static Vector3fc UP;
    @Shadow
    @Final
    private static Vector3fc LEFT;
    @Shadow
    private @Nullable Entity entity;
    @Shadow
    @Final
    private Quaternionf rotation;
    @Shadow
    @Final
    private Vector3f forwards;
    @Shadow
    @Final
    private Vector3f up;
    @Shadow
    @Final
    private Vector3f left;
    @Shadow
    private float xRot;
    @Shadow
    private float yRot;
    @Shadow
    private boolean detached;
    @Shadow
    private float eyeHeight;
    @Shadow
    private float eyeHeightOld;
    @Shadow
    private int matrixPropertiesDirty;
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Shadow
    protected abstract void move(float forwards, float up, float right);

    @Shadow
    private float getMaxZoom(float cameraDist) {
        throw new AssertionError();
    }

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void theabyss$gravityCamera(float partialTicks, CallbackInfo ci) {
        Entity target = this.entity;
        if (target == null || target.isPassenger()) {
            return;
        }
        Quaternionf frame = GravityClient.cameraFrame(target, partialTicks);
        if (frame == null) {
            return;
        }
        Quaternionf local = new Quaternionf().rotationYXZ(
            (float)Math.PI - this.yRot * Mth.DEG_TO_RAD, -this.xRot * Mth.DEG_TO_RAD, -net.yezon.theabyss.platform.services.ClientPlatformHooks.get().cameraRoll((Camera)(Object)this) * Mth.DEG_TO_RAD);
        this.rotation.set(frame).mul(local);
        FORWARDS.rotate(this.rotation, this.forwards);
        UP.rotate(this.rotation, this.up);
        LEFT.rotate(this.rotation, this.left);
        this.matrixPropertiesDirty |= 3;
        this.setPosition(GravityClient.cameraEye(target, partialTicks, Mth.lerp(partialTicks, this.eyeHeightOld, this.eyeHeight)));
        if (this.detached) {
            float distance = 4.0F;
            float scale = 1.0F;
            if (target instanceof LivingEntity living) {
                scale = living.getScale();
                distance = (float)living.getAttributeValue(Attributes.CAMERA_DISTANCE);
            }
            float zoom = net.yezon.theabyss.platform.client.ClientHooks.getDetachedCameraDistance(
                (Camera)(Object)this, this.minecraft.options.getCameraType().isMirrored(), distance, scale, scale, distance);
            this.move(-this.getMaxZoom(zoom), 0.0F, 0.0F);
        }
    }
}
