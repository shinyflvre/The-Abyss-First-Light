package net.yezon.theabyss.platform.client;

import net.minecraft.client.Camera;
import net.yezon.theabyss.platform.services.ClientPlatformHooks;

public final class ClientHooks {
    private ClientHooks() {
    }

    public static float getDetachedCameraDistance(Camera camera, boolean flipped, float entityScale, float entityDistance, float vehicleEntityScale,
        float vehicleDistance) {
        return ClientPlatformHooks.get().detachedCameraDistance(camera, flipped, entityScale, entityDistance, vehicleEntityScale, vehicleDistance);
    }
}
