package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class EntityRenderersEvent extends Event {

    public static abstract class AddLayers extends EntityRenderersEvent {
        public abstract java.util.Set<net.minecraft.world.entity.player.PlayerModelType> getSkins();
        public abstract <R extends net.minecraft.client.renderer.entity.player.AvatarRenderer<net.minecraft.client.player.AbstractClientPlayer>> R getPlayerRenderer(net.minecraft.world.entity.player.PlayerModelType a0);
    }

    public static abstract class RegisterLayerDefinitions extends EntityRenderersEvent {
        public abstract void registerLayerDefinition(net.minecraft.client.model.geom.ModelLayerLocation a0, java.util.function.Supplier<net.minecraft.client.model.geom.builders.LayerDefinition> a1);
    }

    public static abstract class RegisterRenderers extends EntityRenderersEvent {
        public abstract <T extends net.minecraft.world.level.block.entity.BlockEntity, S extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> void registerBlockEntityRenderer(net.minecraft.world.level.block.entity.BlockEntityType<? extends T> a0, net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider<T, S> a1);
        public abstract <T extends net.minecraft.world.entity.Entity> void registerEntityRenderer(net.minecraft.world.entity.EntityType<? extends T> a0, net.minecraft.client.renderer.entity.EntityRendererProvider<T> a1);
    }
}
