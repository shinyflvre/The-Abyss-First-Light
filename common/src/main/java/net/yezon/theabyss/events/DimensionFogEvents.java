package net.yezon.theabyss.events;

import org.jspecify.annotations.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.FogType;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.fx.FogRange;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ViewportEvent.RenderFog;

@EventBusSubscriber(value = Dist.CLIENT)
public class DimensionFogEvents {
    private static final ResourceKey<Level> THE_ABYSS = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("theabyss:the_abyss"));
    private static final ResourceKey<Level> FROST_WORLD = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("theabyss:frost_world"));
    private static final ResourceKey<Level> RIFT_OF_SINS = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("theabyss:riftofsins"));
    private static final float RIFT_FOG_START = 6.0F;
    private static final float RIFT_FOG_END = 72.0F;
    private static final float WATER_FOG_START = -8.0F;
    private static final float WATER_FOG_END = 96.0F;

    private static float rainFog;

    private static void setDistance(RenderFog provider, float start, float end) {
        FogRange.linear(provider, start, end);
    }

    @SubscribeEvent
    public static void renderFog(RenderFog event) {
        if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        if (event.isAtmospheric()) {
            updateRainFog(event, level);
            execute(event, level);
        } else if (event.getType() == FogType.WATER && clearSight(event.getCamera()) && level.dimension().location().getNamespace().equals(TheabyssMod.MODID)) {
            water(event);
        }
    }

    public static void execute(LevelAccessor world) {
        execute(null, world);
    }

    private static void execute(@Nullable RenderFog event, LevelAccessor world) {
        if (event == null) {
            return;
        }

        ResourceKey<Level> dimension = world instanceof Level _lvl ? _lvl.dimension() : Level.OVERWORLD;
        if (dimension == THE_ABYSS) {
            setDistance(event, 32.0F, 64.0F);
        }

        if (dimension == FROST_WORLD) {
            setDistance(event, 10.0F, 32.0F);
        }

        if (dimension == RIFT_OF_SINS) {
            float start = RIFT_FOG_START - 160.0F * rainFog;
            float end = Math.max(Math.min(96.0F, RIFT_FOG_END), RIFT_FOG_END - 256.0F * rainFog);
            if (Minecraft.getInstance().gui.getBossOverlay().shouldCreateWorldFog()) {
                start = Math.min(start, 10.0F);
                end = Math.min(end, 96.0F);
            }
            setDistance(event, start, end);
        }
    }

    private static void water(RenderFog event) {
        float end = WATER_FOG_END;
        if (event.getCamera().getEntity() instanceof LocalPlayer player) {
            end *= Math.max(0.25F, player.getWaterVision());
        }
        setDistance(event, WATER_FOG_START, end);
    }

    private static boolean clearSight(Camera camera) {
        return !(camera.getEntity() instanceof LivingEntity living) || !living.hasEffect(MobEffects.BLINDNESS) && !living.hasEffect(MobEffects.DARKNESS);
    }

    private static void updateRainFog(RenderFog event, ClientLevel level) {
        BlockPos pos = event.getCamera().getBlockPosition();
        boolean rainsInBiome = level.getBiome(pos).value().hasPrecipitation();
        float skyLight = Mth.clamp((level.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue(pos) - 8.0F) / 7.0F, 0.0F, 1.0F);
        float target = level.getRainLevel(event.getPartialTick()) * skyLight * (rainsInBiome ? 1.0F : 0.5F);
        float deltaTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
        rainFog = rainFog + (target - rainFog) * deltaTicks * 0.2F;
    }
}
