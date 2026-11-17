package net.yezon.theabyss.weather.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.RenderFrameEvent;
import net.yezon.theabyss.platform.client.event.ViewportEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.weather.AbyssStorm;
import org.joml.Vector3f;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class AbyssLightningClient {
    private static final Vector3f SKY_COLOUR = new Vector3f(1.0F, 0x59 / 255.0F, 0x59 / 255.0F);
    private static final Vector3f LIGHT_COLOUR = new Vector3f(1.0F, 0x5E / 255.0F, 0x5E / 255.0F);

    private static float flash;

    private AbyssLightningClient() {
    }

    public static float flash() {
        return flash;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || !level.dimension().equals(AbyssStorm.THE_ABYSS) || mc.options.hideLightningFlash().get() || skyFlashTime(level) <= 0) {
            flash = 0.0F;
            return;
        }
        BlockPos camera = mc.gameRenderer.getMainCamera().getBlockPosition();
        float aboveGround = level.getBrightness(LightLayer.SKY, camera) / 15.0F;
        flash = aboveGround;
    }

    public static void tintLightmap(Vector3f colour) {
        if (flash > 0.0F) {
            colour.lerp(LIGHT_COLOUR, flash);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onFogColour(ViewportEvent.ComputeFogColor event) {
        if (flash <= 0.0F) {
            return;
        }
        event.setRed(event.getRed() + (SKY_COLOUR.x() - event.getRed()) * flash);
        event.setGreen(event.getGreen() + (SKY_COLOUR.y() - event.getGreen()) * flash);
        event.setBlue(event.getBlue() + (SKY_COLOUR.z() - event.getBlue()) * flash);
    }

    private static int skyFlashTime(ClientLevel level) {
        return level.getSkyFlashTime();
    }
}
