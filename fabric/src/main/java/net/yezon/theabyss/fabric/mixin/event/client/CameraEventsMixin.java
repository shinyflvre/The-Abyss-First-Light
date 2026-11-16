package net.yezon.theabyss.fabric.mixin.event.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.yezon.theabyss.fabric.client.FabricCameraRoll;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import net.yezon.theabyss.platform.client.event.ViewportEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraEventsMixin {
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
    private int matrixPropertiesDirty;

    @Inject(method = "setRotation(FF)V", at = @At("HEAD"))
    private void theabyss$resetRoll(float yRot, float xRot, CallbackInfo ci) {
        FabricCameraRoll.set((Camera)(Object)this, 0.0F);
    }

    @WrapOperation(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 1))
    private void theabyss$cameraAngles(Camera camera, float yaw, float pitch, Operation<Void> original, @Local(argsOnly = true) float partialTicks) {
        ViewportEvent.ComputeCameraAngles event = ClientFabricEvents.cameraAngles(partialTicks, yaw, pitch);
        if (event == null) {
            original.call(camera, yaw, pitch);
            return;
        }
        original.call(camera, event.getYaw(), event.getPitch());
        this.theabyss$applyRoll(event.getRoll());
    }

    @WrapOperation(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 2))
    private void theabyss$mirroredRoll(Camera camera, float yaw, float pitch, Operation<Void> original) {
        float roll = FabricCameraRoll.get(camera);
        original.call(camera, yaw, pitch);
        this.theabyss$applyRoll(-roll);
    }

    @ModifyReturnValue(method = "modifyFovBasedOnDeathOrFluid", at = @At("RETURN"))
    private float theabyss$computeFov(float fov, @Local(argsOnly = true, ordinal = 0) float partialTicks) {
        return ClientFabricEvents.fov((Camera)(Object)this, partialTicks, fov);
    }

    private void theabyss$applyRoll(float roll) {
        if (roll == 0.0F) {
            return;
        }
        FabricCameraRoll.set((Camera)(Object)this, roll);
        this.rotation.rotationYXZ((float)Math.PI - this.yRot * (float)(Math.PI / 180.0), -this.xRot * (float)(Math.PI / 180.0), -roll * (float)(Math.PI / 180.0));
        FORWARDS.rotate(this.rotation, this.forwards);
        UP.rotate(this.rotation, this.up);
        LEFT.rotate(this.rotation, this.left);
        this.matrixPropertiesDirty |= 3;
    }
}
