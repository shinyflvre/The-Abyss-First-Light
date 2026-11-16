package net.yezon.theabyss.events;

import org.jspecify.annotations.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ViewportEvent.RenderFog;

@EventBusSubscriber(value = Dist.CLIENT)
public class DimensionFogEvents {
    private static final ResourceKey<Level> THE_ABYSS = ResourceKey.create(Registries.DIMENSION, Identifier.parse("theabyss:the_abyss"));
    private static final ResourceKey<Level> FROST_WORLD = ResourceKey.create(Registries.DIMENSION, Identifier.parse("theabyss:frost_world"));

    private static void setDistance(RenderFog provider, float start, float end) {
        provider.setNearPlaneDistance(start);
        provider.setFarPlaneDistance(end);
    }

    @SubscribeEvent
    public static void renderFog(RenderFog event) {
        if (event.getEnvironment() instanceof AtmosphericFogEnvironment) {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                execute(event, level);
            }
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
    }
}
