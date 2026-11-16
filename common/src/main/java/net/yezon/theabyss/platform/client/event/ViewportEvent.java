package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class ViewportEvent extends Event {

    public static class ComputeCameraAngles extends ViewportEvent {
        private final float partialTick;
        private float pitch;
        private float roll;
        private float yaw;

        public ComputeCameraAngles(float partialTick, float pitch, float roll, float yaw) {
            this.partialTick = partialTick;
            this.pitch = pitch;
            this.roll = roll;
            this.yaw = yaw;
        }

        public float getPartialTick() {
            return this.partialTick;
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
        private float blue;
        private float green;
        private float red;

        public ComputeFogColor(float blue, float green, float red) {
            this.blue = blue;
            this.green = green;
            this.red = red;
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
        private final net.minecraft.client.Camera camera;
        private float fOV;
        private final float partialTick;

        public ComputeFov(net.minecraft.client.Camera camera, float fOV, float partialTick) {
            this.camera = camera;
            this.fOV = fOV;
            this.partialTick = partialTick;
        }

        public net.minecraft.client.Camera getCamera() {
            return this.camera;
        }

        public float getFOV() {
            return this.fOV;
        }

        public void setFOV(float value) {
            this.fOV = value;
        }

        public float getPartialTick() {
            return this.partialTick;
        }
    }

    public static class RenderFog extends ViewportEvent {
        private final net.minecraft.client.renderer.fog.environment.FogEnvironment environment;
        private float farPlaneDistance;
        private float nearPlaneDistance;

        public RenderFog(net.minecraft.client.renderer.fog.environment.FogEnvironment environment, float farPlaneDistance, float nearPlaneDistance) {
            this.environment = environment;
            this.farPlaneDistance = farPlaneDistance;
            this.nearPlaneDistance = nearPlaneDistance;
        }

        public net.minecraft.client.renderer.fog.environment.FogEnvironment getEnvironment() {
            return this.environment;
        }

        public float getFarPlaneDistance() {
            return this.farPlaneDistance;
        }

        public void setFarPlaneDistance(float value) {
            this.farPlaneDistance = value;
        }

        public float getNearPlaneDistance() {
            return this.nearPlaneDistance;
        }

        public void setNearPlaneDistance(float value) {
            this.nearPlaneDistance = value;
        }
    }
}
