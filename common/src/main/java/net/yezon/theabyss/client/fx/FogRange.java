package net.yezon.theabyss.client.fx;

import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Minecraft;
import net.yezon.theabyss.platform.client.event.ViewportEvent;

public final class FogRange {
    private static final float SPREAD = 0.133F;

    private FogRange() {
    }

    public static void linear(ViewportEvent.RenderFog event, float start, float end) {
        float span = Math.max(0.0F, end - start);
        float near = start - span * SPREAD;
        float far = end + span * SPREAD;
        float limit = Math.max(Minecraft.getInstance().gameRenderer.getRenderDistance(), 32.0F);
        if (far > limit) {
            far = limit;
        }
        event.setNearPlaneDistance(Math.min(near, far - 1.0F));
        event.setFarPlaneDistance(far);
        event.setFogShape(FogShape.SPHERE);
    }
}
