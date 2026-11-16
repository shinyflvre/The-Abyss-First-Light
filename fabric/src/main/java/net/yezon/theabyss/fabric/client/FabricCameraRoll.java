package net.yezon.theabyss.fabric.client;

import net.minecraft.client.Camera;

public final class FabricCameraRoll {
    private static float roll;

    private FabricCameraRoll() {
    }

    public static float get(Camera camera) {
        return roll;
    }

    public static void set(Camera camera, float value) {
        roll = value;
    }
}
