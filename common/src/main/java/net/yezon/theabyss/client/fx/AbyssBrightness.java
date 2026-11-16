package net.yezon.theabyss.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ExtractLevelRenderStateEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.configuration.ConfigConfiguration;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class AbyssBrightness {
    private static final ResourceKey<Level> ABYSS = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "the_abyss"));

    private AbyssBrightness() {
    }

    @SubscribeEvent
    public static void onExtract(ExtractLevelRenderStateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        boolean rift = net.yezon.theabyss.rift.RiftOfSins.isRift(mc.level);
        if (!rift && mc.level.dimension() != ABYSS) {
            return;
        }
        LightmapRenderState lightmap = mc.gameRenderer.gameRenderState().lightmapRenderState;
        float gamma = mc.options.gamma().get().floatValue();
        float darkness = Math.max(0.0F, gamma - lightmap.brightness);
        float wanted = rift ? 0.0F : ConfigConfiguration.ABYSS_BRIGHTNESS.get() / 100.0F;
        lightmap.brightness = Math.max(0.0F, wanted - darkness);
    }
}
