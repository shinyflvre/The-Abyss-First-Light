package net.yezon.theabyss.fabric.client;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.yezon.theabyss.client.renderer.BlockRenderLayers;
import net.yezon.theabyss.platform.client.event.EntityRenderersEvent;
import net.yezon.theabyss.platform.event.AbyssEventBus;

public final class FabricRenderRegistrations {
    private FabricRenderRegistrations() {
    }

    public static void register() {
        BlockRenderLayers.forEach(BlockRenderLayerMap.INSTANCE::putBlock);
        FabricModelExtras.register();
        AbyssEventBus.post(new EntityRenderersEvent.RegisterLayerDefinitions() {
            @Override
            public void registerLayerDefinition(ModelLayerLocation location, Supplier<LayerDefinition> definition) {
                EntityModelLayerRegistry.registerModelLayer(location, definition::get);
            }
        });
        AbyssEventBus.post(new EntityRenderersEvent.RegisterRenderers() {
            @Override
            public <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> provider) {
                BlockEntityRenderers.register(type, provider);
            }

            @Override
            public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                EntityRendererRegistry.register(type, provider);
            }
        });
    }

    public static void addLayers(Map<PlayerSkin.Model, EntityRenderer<? extends Player>> playerRenderers) {
        AbyssEventBus.post(new EntityRenderersEvent.AddLayers() {
            @Override
            public Set<PlayerSkin.Model> getSkins() {
                return playerRenderers.keySet();
            }

            @Override
            @SuppressWarnings("unchecked")
            public <R extends EntityRenderer<? extends Player>> R getSkin(PlayerSkin.Model skin) {
                return (R)playerRenderers.get(skin);
            }
        });
    }
}
