package net.yezon.theabyss.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.configuration.ConfigConfiguration;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class AbyssBrightness {
    private static final ResourceKey<Level> ABYSS = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "the_abyss"));

    private AbyssBrightness() {
    }

    public static float brightness(float brightness) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return brightness;
        }
        boolean rift = net.yezon.theabyss.rift.RiftOfSins.isRift(mc.level);
        if (!rift && mc.level.dimension() != ABYSS) {
            return brightness;
        }
        float gamma = mc.options.gamma().get().floatValue();
        float darkness = Math.max(0.0F, gamma - brightness);
        float wanted = rift ? 0.0F : ConfigConfiguration.ABYSS_BRIGHTNESS.get() / 100.0F;
        return Math.max(0.0F, wanted - darkness);
    }
}
