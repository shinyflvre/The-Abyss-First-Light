package net.yezon.theabyss.weather.client;

import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.weather.AbyssStorm;
import org.joml.Vector3f;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class AbyssLightningClient {
    private static final Vector3f SKY_COLOUR = new Vector3f(1.0F, 0x59 / 255.0F, 0x59 / 255.0F);
    private static final Vector3f LIGHT_COLOUR = new Vector3f(1.0F, 0x5E / 255.0F, 0x5E / 255.0F);

    private static float flash;
    private static Field skyFlashField;
    private static boolean reflectionFailed;

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
        BlockPos camera = mc.gameRenderer.mainCamera().blockPosition();
        float aboveGround = level.getBrightness(LightLayer.SKY, camera) / 15.0F;
        flash = aboveGround;
    }

    @SubscribeEvent
    public static void onExtractLevel(ExtractLevelRenderStateEvent event) {
        LightmapRenderState lightmap = Minecraft.getInstance().gameRenderer.gameRenderState().lightmapRenderState;
        if (flash <= 0.0F || !lightmap.needsUpdate) {
            return;
        }
        lightmap.ambientColor = new Vector3f(lightmap.ambientColor).lerp(LIGHT_COLOUR, flash);
        lightmap.skyLightColor = new Vector3f(lightmap.skyLightColor).lerp(LIGHT_COLOUR, flash);
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
        if (reflectionFailed) {
            return 0;
        }
        try {
            if (skyFlashField == null) {
                skyFlashField = ClientLevel.class.getDeclaredField("skyFlashTime");
                skyFlashField.setAccessible(true);
            }
            return skyFlashField.getInt(level);
        } catch (ReflectiveOperationException | RuntimeException e) {
            reflectionFailed = true;
            TheabyssMod.LOGGER.warn("[AbyssLightning] cannot read the sky flash, lightning colour disabled", e);
            return 0;
        }
    }
}
