package net.yezon.theabyss.platform.client.event;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import com.mojang.blaze3d.shaders.FogShape;
import net.yezon.theabyss.platform.event.Event;

public abstract class ViewportEvent extends Event {
    private final Camera camera;
    private final float partialTick;

    protected ViewportEvent(Camera camera, float partialTick) {
        this.camera = camera;
        this.partialTick = partialTick;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public float getPartialTick() {
        return this.partialTick;
    }

    public static class ComputeCameraAngles extends ViewportEvent {
        private float yaw;
        private float pitch;
        private float roll;

        public ComputeCameraAngles(Camera camera, float partialTick, float yaw, float pitch, float roll) {
            super(camera, partialTick);
            this.yaw = yaw;
            this.pitch = pitch;
            this.roll = roll;
        }

        public float getPitch() {
            return this.pitch;
        }

        public void setPitch(float value) {
            this.pitch = value;
        }

        public float getRoll() {
            return this.roll;
        }

        public void setRoll(float value) {
            this.roll = value;
        }

        public float getYaw() {
            return this.yaw;
        }

        public void setYaw(float value) {
            this.yaw = value;
        }
    }

    public static class ComputeFogColor extends ViewportEvent {
        private float red;
        private float green;
        private float blue;

        public ComputeFogColor(Camera camera, float partialTick, float red, float green, float blue) {
            super(camera, partialTick);
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public float getBlue() {
            return this.blue;
        }

        public void setBlue(float value) {
            this.blue = value;
        }

        public float getGreen() {
            return this.green;
        }

        public void setGreen(float value) {
            this.green = value;
        }

        public float getRed() {
            return this.red;
        }

        public void setRed(float value) {
            this.red = value;
        }
    }

    public static class ComputeFov extends ViewportEvent {
        private float fov;
        private final boolean usedConfiguredFov;

        public ComputeFov(Camera camera, float partialTick, float fov, boolean usedConfiguredFov) {
            super(camera, partialTick);
            this.fov = fov;
            this.usedConfiguredFov = usedConfiguredFov;
        }

        public float getFOV() {
            return this.fov;
        }

        public void setFOV(float value) {
            this.fov = value;
        }

        public boolean usedConfiguredFov() {
            return this.usedConfiguredFov;
        }
    }

    public static class RenderFog extends ViewportEvent {
        private final FogRenderer.FogMode mode;
        private final FogType type;
        private float nearPlaneDistance;
        private float farPlaneDistance;
        private FogShape fogShape;
        private boolean changed;

        public RenderFog(FogRenderer.FogMode mode, FogType type, Camera camera, float partialTick, float nearPlaneDistance, float farPlaneDistance, FogShape fogShape) {
            super(camera, partialTick);
            this.mode = mode;
            this.type = type;
            this.nearPlaneDistance = nearPlaneDistance;
            this.farPlaneDistance = farPlaneDistance;
            this.fogShape = fogShape;
        }

        public FogRenderer.FogMode getMode() {
            return this.mode;
        }

        public FogType getType() {
            return this.type;
        }

        public boolean isAtmospheric() {
            if (this.type != FogType.NONE) {
                return false;
            }
            return !(this.getCamera().getEntity() instanceof LivingEntity living)
                || !living.hasEffect(MobEffects.BLINDNESS) && !living.hasEffect(MobEffects.DARKNESS);
        }

        public float getFarPlaneDistance() {
            return this.farPlaneDistance;
        }

        public void setFarPlaneDistance(float value) {
            this.farPlaneDistance = value;
            this.changed = true;
        }

        public float getNearPlaneDistance() {
            return this.nearPlaneDistance;
        }

        public void setNearPlaneDistance(float value) {
            this.nearPlaneDistance = value;
            this.changed = true;
        }

        public FogShape getFogShape() {
            return this.fogShape;
        }

        public void setFogShape(FogShape value) {
            this.fogShape = value;
            this.changed = true;
        }

        public boolean isChanged() {
            return this.changed;
        }
    }
}
