package net.yezon.theabyss.fabric.client;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.particle.v1.ParticleGroupRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.yezon.theabyss.fabric.event.FabricHudLayers;
import net.yezon.theabyss.mixin.accessor.client.RenderPipelinesAccessor;
import net.yezon.theabyss.platform.client.CustomSkyboxRenderer;
import net.yezon.theabyss.platform.client.event.EntityRenderersEvent;
import net.yezon.theabyss.platform.client.event.RegisterColorHandlersEvent;
import net.yezon.theabyss.platform.client.event.RegisterCustomEnvironmentEffectRendererEvent;
import net.yezon.theabyss.platform.client.event.RegisterGuiLayersEvent;
import net.yezon.theabyss.platform.client.event.RegisterKeyMappingsEvent;
import net.yezon.theabyss.platform.client.event.RegisterMenuScreensEvent;
import net.yezon.theabyss.platform.client.event.RegisterParticleGroupsEvent;
import net.yezon.theabyss.platform.client.event.RegisterParticleProvidersEvent;
import net.yezon.theabyss.platform.client.event.RegisterPictureInPictureRenderersEvent;
import net.yezon.theabyss.platform.client.event.RegisterRenderPipelinesEvent;
import net.yezon.theabyss.platform.client.event.RegisterSelectItemModelPropertyEvent;
import net.yezon.theabyss.platform.client.event.RegisterSpecialModelRendererEvent;
import net.yezon.theabyss.platform.client.gui.GuiLayer;
import net.yezon.theabyss.platform.client.gui.VanillaGuiLayers;
import net.yezon.theabyss.platform.client.renderstate.RegisterRenderStateModifiersEvent;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import org.jspecify.annotations.Nullable;

public final class FabricClientRegistrations {
    private static final Map<Identifier, CustomSkyboxRenderer> SKYBOXES = new ConcurrentHashMap<>();

    private FabricClientRegistrations() {
    }

    public static void register() {
        AbyssEventBus.post(new RegisterRenderPipelinesEvent() {
            @Override
            public void registerPipeline(RenderPipeline pipeline) {
                if (RenderPipelinesAccessor.theabyss$pipelinesByLocation().putIfAbsent(pipeline.getLocation(), pipeline) != null) {
                    throw new IllegalStateException("Duplicate RenderPipeline registration for location: " + pipeline.getLocation());
                }
            }
        });
        AbyssEventBus.post(new EntityRenderersEvent.RegisterLayerDefinitions() {
            @Override
            public void registerLayerDefinition(ModelLayerLocation location, Supplier<LayerDefinition> definition) {
                ModelLayerRegistry.registerModelLayer(location, definition::get);
            }
        });
        AbyssEventBus.post(new EntityRenderersEvent.RegisterRenderers() {
            @Override
            @SuppressWarnings("unchecked")
            public <T extends BlockEntity, S extends BlockEntityRenderState> void registerBlockEntityRenderer(BlockEntityType<? extends T> type,
                BlockEntityRendererProvider<T, S> provider) {
                BlockEntityRendererRegistry.register((BlockEntityType<T>)type, provider);
            }

            @Override
            public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                EntityRendererRegistry.register(type, provider);
            }
        });
        AbyssEventBus.post(new RegisterKeyMappingsEvent() {
            @Override
            public void register(KeyMapping key) {
                KeyMappingHelper.registerKeyMapping(key);
            }

            @Override
            public void registerCategory(KeyMapping.Category category) {
                KeyMapping.Category.register(category.id());
            }
        });
        AbyssEventBus.post(new RegisterMenuScreensEvent() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type,
                ScreenConstructor<M, U> constructor) {
                MenuScreens.<M, U>register(type, constructor::create);
            }
        });
        AbyssEventBus.post(new RegisterParticleProvidersEvent() {
            @Override
            public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type, SpriteParticleRegistration<T> registration) {
                ParticleProviderRegistry.getInstance().register(type, registration::create);
            }
        });
        AbyssEventBus.post(new RegisterParticleGroupsEvent() {
            @Override
            public void register(ParticleRenderType group, Function<ParticleEngine, ParticleGroup<?>> factory) {
                ParticleGroupRegistry.register(group, factory);
            }
        });
        AbyssEventBus.post(new RegisterPictureInPictureRenderersEvent() {
            @Override
            public <T extends PictureInPictureRenderState> void register(Class<T> type, Supplier<PictureInPictureRenderer<T>> factory) {
                PictureInPictureRendererRegistry.register(context -> factory.get());
            }
        });
        AbyssEventBus.post(new RegisterSelectItemModelPropertyEvent() {
            @Override
            public void register(Identifier id, SelectItemModelProperty.Type<?, ?> type) {
                SelectItemModelProperties.ID_MAPPER.put(id, type);
            }
        });
        AbyssEventBus.post(new RegisterSpecialModelRendererEvent() {
            @Override
            public void register(Identifier id, MapCodec<? extends SpecialModelRenderer.Unbaked<?>> codec) {
                SpecialModelRenderers.ID_MAPPER.put(id, codec);
            }
        });
        AbyssEventBus.post(new RegisterGuiLayersEvent() {
            @Override
            public void registerAbove(Identifier other, Identifier id, GuiLayer layer) {
                FabricHudLayers.above(other, id, layer);
                HudElementRegistry.attachElementAfter(hudElement(other), id, (graphics, delta) -> FabricHudLayers.render(id, layer, graphics, delta));
            }

            @Override
            public void registerBelow(Identifier other, Identifier id, GuiLayer layer) {
                FabricHudLayers.below(other, id, layer);
                if (VanillaGuiLayers.AIR_LEVEL.equals(other)) {
                    HudElementRegistry.attachElementAfter(VanillaHudElements.MOUNT_HEALTH, id, (graphics, delta) -> FabricHudLayers.render(id, layer, graphics, delta));
                } else {
                    HudElementRegistry.attachElementBefore(hudElement(other), id, (graphics, delta) -> FabricHudLayers.render(id, layer, graphics, delta));
                }
            }
        });
        AbyssEventBus.post(new RegisterCustomEnvironmentEffectRendererEvent() {
            @Override
            public void registerSkyboxRenderer(Identifier id, CustomSkyboxRenderer renderer) {
                if (SKYBOXES.putIfAbsent(id, renderer) != null) {
                    throw new IllegalStateException("Duplicate skybox renderer registration for " + id);
                }
            }
        });
        AbyssEventBus.post(new RegisterRenderStateModifiersEvent() {
            @Override
            public <E extends Entity, S extends EntityRenderState> void registerEntityModifier(
                Class<? extends EntityRenderer<? extends E, ? extends S>> baseRenderer, BiConsumer<E, S> modifier) {
                FabricRenderStateModifiers.register(baseRenderer, modifier);
            }
        });
    }

    public static void addLayers(Map<PlayerModelType, AvatarRenderer<AbstractClientPlayer>> playerRenderers) {
        AbyssEventBus.post(new EntityRenderersEvent.AddLayers() {
            @Override
            public Set<PlayerModelType> getSkins() {
                return playerRenderers.keySet();
            }

            @Override
            @SuppressWarnings("unchecked")
            public <R extends AvatarRenderer<AbstractClientPlayer>> R getPlayerRenderer(PlayerModelType skin) {
                return (R)playerRenderers.get(skin);
            }
        });
    }

    public static void blockTintSources(BlockColors colors) {
        AbyssEventBus.post(new RegisterColorHandlersEvent.BlockTintSources() {
            @Override
            public BlockColors getBlockColors() {
                return colors;
            }

            @Override
            public void register(List<BlockTintSource> sources, Block... blocks) {
                colors.register(sources, blocks);
            }
        });
    }

    public static @Nullable CustomSkyboxRenderer skybox(@Nullable ClientLevel level) {
        if (level == null || SKYBOXES.isEmpty()) {
            return null;
        }
        return level.dimensionTypeRegistration().unwrapKey().map(ResourceKey::identifier).map(SKYBOXES::get).orElse(null);
    }

    private static Identifier hudElement(Identifier layer) {
        if (VanillaGuiLayers.CAMERA_OVERLAYS.equals(layer)) {
            return VanillaHudElements.MISC_OVERLAYS;
        }
        if (VanillaGuiLayers.CROSSHAIR.equals(layer)) {
            return VanillaHudElements.CROSSHAIR;
        }
        if (VanillaGuiLayers.PLAYER_HEALTH.equals(layer)) {
            return VanillaHudElements.HEALTH_BAR;
        }
        if (VanillaGuiLayers.VEHICLE_HEALTH.equals(layer)) {
            return VanillaHudElements.MOUNT_HEALTH;
        }
        if (VanillaGuiLayers.AIR_LEVEL.equals(layer)) {
            return VanillaHudElements.AIR_BAR;
        }
        return layer;
    }
}
