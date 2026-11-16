package net.yezon.theabyss.neoforge.event;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.event.EventPriority;

public final class NeoForgeClientEventBridgeGenerated {
    private NeoForgeClientEventBridgeGenerated() {
    }

    static net.neoforged.bus.api.EventPriority priority(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> net.neoforged.bus.api.EventPriority.HIGHEST;
            case HIGH -> net.neoforged.bus.api.EventPriority.HIGH;
            case NORMAL -> net.neoforged.bus.api.EventPriority.NORMAL;
            case LOW -> net.neoforged.bus.api.EventPriority.LOW;
            case LOWEST -> net.neoforged.bus.api.EventPriority.LOWEST;
        };
    }

    public static void register(IEventBus bus) {
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.fml.event.lifecycle.FMLClientSetupEvent.class)) {
            bus.addListener(priority(p), true, net.neoforged.fml.event.lifecycle.FMLClientSetupEvent.class, e -> {
                net.yezon.theabyss.platform.fml.event.lifecycle.FMLClientSetupEvent c = new net.yezon.theabyss.platform.fml.event.lifecycle.FMLClientSetupEvent();
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.AddSectionGeometryEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.AddSectionGeometryEvent.class, e -> {
                net.yezon.theabyss.platform.client.event.AddSectionGeometryEvent c = new net.yezon.theabyss.platform.client.event.AddSectionGeometryEvent(e.getLevel(), e.getSectionOrigin());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.ClientPlayerNetworkEvent.LoggingIn.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn.class, e -> {
                net.yezon.theabyss.platform.client.event.ClientPlayerNetworkEvent.LoggingIn c = new net.yezon.theabyss.platform.client.event.ClientPlayerNetworkEvent.LoggingIn();
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.ClientPlayerNetworkEvent.LoggingOut.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut.class, e -> {
                net.yezon.theabyss.platform.client.event.ClientPlayerNetworkEvent.LoggingOut c = new net.yezon.theabyss.platform.client.event.ClientPlayerNetworkEvent.LoggingOut();
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.ClientTickEvent.Post.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.ClientTickEvent.Post.class, e -> {
                net.yezon.theabyss.platform.client.event.ClientTickEvent.Post c = new net.yezon.theabyss.platform.client.event.ClientTickEvent.Post();
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.ClientTickEvent.Pre.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.ClientTickEvent.Pre.class, e -> {
                net.yezon.theabyss.platform.client.event.ClientTickEvent.Pre c = new net.yezon.theabyss.platform.client.event.ClientTickEvent.Pre();
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.ComputeFovModifierEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.ComputeFovModifierEvent.class, e -> {
                net.yezon.theabyss.platform.client.event.ComputeFovModifierEvent c = new net.yezon.theabyss.platform.client.event.ComputeFovModifierEvent(e.getNewFovModifier(), e.getPlayer());
                AbyssEventBus.post(p, c);
                e.setNewFovModifier(c.getNewFovModifier());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.CustomizeGuiOverlayEvent.BossEventProgress.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent.BossEventProgress.class, e -> {
                net.yezon.theabyss.platform.client.event.CustomizeGuiOverlayEvent.BossEventProgress c = new net.yezon.theabyss.platform.client.event.CustomizeGuiOverlayEvent.BossEventProgress(e.getBossEvent(), e.getGuiGraphics(), e.getX(), e.getY());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.client.event.EntityRenderersEvent.AddLayers.class)) {
            bus.addListener(net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.client.event.EntityRenderersEvent.AddLayers() {
                @Override
                public java.util.Set<net.minecraft.world.entity.player.PlayerModelType> getSkins() {
                    return e.getSkins();
                }
                @Override
                public <R extends net.minecraft.client.renderer.entity.player.AvatarRenderer<net.minecraft.client.player.AbstractClientPlayer>> R getPlayerRenderer(net.minecraft.world.entity.player.PlayerModelType a0) {
                    return e.getPlayerRenderer(a0);
                }
            }));
        }
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.client.event.EntityRenderersEvent.RegisterLayerDefinitions.class)) {
            bus.addListener(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.client.event.EntityRenderersEvent.RegisterLayerDefinitions() {
                @Override
                public void registerLayerDefinition(net.minecraft.client.model.geom.ModelLayerLocation a0, java.util.function.Supplier<net.minecraft.client.model.geom.builders.LayerDefinition> a1) {
                    e.registerLayerDefinition(a0, a1);
                }
            }));
        }
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.client.event.EntityRenderersEvent.RegisterRenderers.class)) {
            bus.addListener(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.client.event.EntityRenderersEvent.RegisterRenderers() {
                @Override
                public <T extends net.minecraft.world.level.block.entity.BlockEntity, S extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> void registerBlockEntityRenderer(net.minecraft.world.level.block.entity.BlockEntityType<? extends T> a0, net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider<T, S> a1) {
                    e.registerBlockEntityRenderer(a0, a1);
                }
                @Override
                public <T extends net.minecraft.world.entity.Entity> void registerEntityRenderer(net.minecraft.world.entity.EntityType<? extends T> a0, net.minecraft.client.renderer.entity.EntityRendererProvider<T> a1) {
                    e.registerEntityRenderer(a0, a1);
                }
            }));
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.ExtractLevelRenderStateEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent.class, e -> {
                net.yezon.theabyss.platform.client.event.ExtractLevelRenderStateEvent c = new net.yezon.theabyss.platform.client.event.ExtractLevelRenderStateEvent(e.getCamera(), e.getDeltaTracker(), e.getFrustum(), e.getLevel(), e.getOptionsRenderState(), e.getRenderState());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.InputEvent.InteractionKeyMappingTriggered.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered.class, e -> {
                net.yezon.theabyss.platform.client.event.InputEvent.InteractionKeyMappingTriggered c = new net.yezon.theabyss.platform.client.event.InputEvent.InteractionKeyMappingTriggered(e.getHand(), e.isAttack(), e.isPickBlock(), e.isUseItem(), e.shouldSwingHand());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setSwingHand(c.getSwingHand());
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.InputEvent.MouseScrollingEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent.class, e -> {
                net.yezon.theabyss.platform.client.event.InputEvent.MouseScrollingEvent c = new net.yezon.theabyss.platform.client.event.InputEvent.MouseScrollingEvent(e.getScrollDeltaY());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.ModelEvent.ModifyBakingResult.class)) {
            bus.addListener(priority(p), true, net.neoforged.neoforge.client.event.ModelEvent.ModifyBakingResult.class, e -> {
                net.yezon.theabyss.platform.client.event.ModelEvent.ModifyBakingResult c = new net.yezon.theabyss.platform.client.event.ModelEvent.ModifyBakingResult(e.getBakingResult());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.MovementInputUpdateEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.MovementInputUpdateEvent.class, e -> {
                net.yezon.theabyss.platform.client.event.MovementInputUpdateEvent c = new net.yezon.theabyss.platform.client.event.MovementInputUpdateEvent(e.getInput());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.RecipesReceivedEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RecipesReceivedEvent.class, e -> {
                net.yezon.theabyss.platform.client.event.RecipesReceivedEvent c = new net.yezon.theabyss.platform.client.event.RecipesReceivedEvent(e.getRecipeMap());
                AbyssEventBus.post(p, c);
            });
        }
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.client.event.RegisterClientCommandsEvent.class)) {
            NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.client.event.RegisterClientCommandsEvent.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.client.event.RegisterClientCommandsEvent() {
                @Override
                public com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> getDispatcher() {
                    return e.getDispatcher();
                }
            }));
        }
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.client.event.RegisterColorHandlersEvent.BlockTintSources.class)) {
            bus.addListener(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.BlockTintSources.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.client.event.RegisterColorHandlersEvent.BlockTintSources() {
                @Override
                public net.minecraft.client.color.block.BlockColors getBlockColors() {
                    return e.getBlockColors();
                }
                @Override
                public void register(java.util.List<net.minecraft.client.color.block.BlockTintSource> a0, net.minecraft.world.level.block.Block... a1) {
                    e.register(a0, a1);
                }
            }));
        }
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.client.event.RegisterKeyMappingsEvent.class)) {
            bus.addListener(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.client.event.RegisterKeyMappingsEvent() {
                @Override
                public void register(net.minecraft.client.KeyMapping a0) {
                    e.register(a0);
                }
                @Override
                public void registerCategory(net.minecraft.client.KeyMapping.Category a0) {
                    e.registerCategory(a0);
                }
            }));
        }
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.client.event.RegisterParticleGroupsEvent.class)) {
            bus.addListener(net.neoforged.neoforge.client.event.RegisterParticleGroupsEvent.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.client.event.RegisterParticleGroupsEvent() {
                @Override
                public void register(net.minecraft.client.particle.ParticleRenderType a0, java.util.function.Function<net.minecraft.client.particle.ParticleEngine, net.minecraft.client.particle.ParticleGroup<?>> a1) {
                    e.register(a0, a1);
                }
            }));
        }
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.client.event.RegisterPictureInPictureRenderersEvent.class)) {
            bus.addListener(net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.client.event.RegisterPictureInPictureRenderersEvent() {
                @Override
                public <T extends net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState> void register(Class<T> a0, java.util.function.Supplier<net.minecraft.client.gui.render.pip.PictureInPictureRenderer<T>> a1) {
                    e.register(a0, a1);
                }
            }));
        }
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.client.event.RegisterRenderPipelinesEvent.class)) {
            bus.addListener(net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.client.event.RegisterRenderPipelinesEvent() {
                @Override
                public void registerPipeline(com.mojang.renderpearl.api.pipeline.RenderPipeline a0) {
                    e.registerPipeline(a0);
                }
            }));
        }
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.client.event.RegisterSelectItemModelPropertyEvent.class)) {
            bus.addListener(net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.client.event.RegisterSelectItemModelPropertyEvent() {
                @Override
                public void register(net.minecraft.resources.Identifier a0, net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty.Type<?, ?> a1) {
                    e.register(a0, a1);
                }
            }));
        }
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.client.event.RegisterSpecialModelRendererEvent.class)) {
            bus.addListener(net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.client.event.RegisterSpecialModelRendererEvent() {
                @Override
                public void register(net.minecraft.resources.Identifier a0, com.mojang.serialization.MapCodec<? extends net.minecraft.client.renderer.special.SpecialModelRenderer.Unbaked<?>> a1) {
                    e.register(a0, a1);
                }
            }));
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.RenderArmEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderArmEvent.class, e -> {
                net.yezon.theabyss.platform.client.event.RenderArmEvent c = new net.yezon.theabyss.platform.client.event.RenderArmEvent(e.getArm(), e.getLightCoords(), e.getPlayerRenderState(), e.getPoseStack(), e.getSkinTexture(), e.getSubmitNodeCollector(), e.hasSleeve());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.RenderFrameEvent.Pre.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderFrameEvent.Pre.class, e -> {
                net.yezon.theabyss.platform.client.event.RenderFrameEvent.Pre c = new net.yezon.theabyss.platform.client.event.RenderFrameEvent.Pre(e.getPartialTick());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.RenderGuiEvent.Post.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderGuiEvent.Post.class, e -> {
                net.yezon.theabyss.platform.client.event.RenderGuiEvent.Post c = new net.yezon.theabyss.platform.client.event.RenderGuiEvent.Post();
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.RenderGuiEvent.Pre.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderGuiEvent.Pre.class, e -> {
                net.yezon.theabyss.platform.client.event.RenderGuiEvent.Pre c = new net.yezon.theabyss.platform.client.event.RenderGuiEvent.Pre();
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.RenderGuiLayerEvent.Post.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Post.class, e -> {
                net.yezon.theabyss.platform.client.event.RenderGuiLayerEvent.Post c = new net.yezon.theabyss.platform.client.event.RenderGuiLayerEvent.Post(e.getGuiGraphics(), e.getName());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.RenderGuiLayerEvent.Pre.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre.class, e -> {
                net.yezon.theabyss.platform.client.event.RenderGuiLayerEvent.Pre c = new net.yezon.theabyss.platform.client.event.RenderGuiLayerEvent.Pre(e.getGuiGraphics(), e.getName());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.RenderHandEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderHandEvent.class, e -> {
                net.yezon.theabyss.platform.client.event.RenderHandEvent c = new net.yezon.theabyss.platform.client.event.RenderHandEvent(e.getEquipProgress(), e.getHand(), e.getItemStack(), e.getPackedLight(), e.getPlayerState(), e.getPoseStack(), e.getSubmitNodeCollector(), e.getSwingProgress());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.RenderLevelStageEvent.AfterOpaqueBlocks.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterOpaqueBlocks.class, e -> {
                net.yezon.theabyss.platform.client.event.RenderLevelStageEvent.AfterOpaqueBlocks c = new net.yezon.theabyss.platform.client.event.RenderLevelStageEvent.AfterOpaqueBlocks(e.getLevelRenderState(), e.getModelViewMatrix(), e.getRenderPass());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.RenderLivingEvent.Pre.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderLivingEvent.Pre.class, e -> {
                net.yezon.theabyss.platform.client.event.RenderLivingEvent.Pre c = new net.yezon.theabyss.platform.client.event.RenderLivingEvent.Pre(e.getPoseStack(), e.getRenderState());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.RenderPlayerEvent.Post.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderPlayerEvent.Post.class, e -> {
                net.yezon.theabyss.platform.client.event.RenderPlayerEvent.Post c = new net.yezon.theabyss.platform.client.event.RenderPlayerEvent.Post(e.getPoseStack(), e.getRenderState());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.RenderPlayerEvent.Pre.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderPlayerEvent.Pre.class, e -> {
                net.yezon.theabyss.platform.client.event.RenderPlayerEvent.Pre c = new net.yezon.theabyss.platform.client.event.RenderPlayerEvent.Pre(e.getPoseStack(), e.getRenderState());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.ScreenEvent.Render.Post.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.ScreenEvent.Render.Post.class, e -> {
                net.yezon.theabyss.platform.client.event.ScreenEvent.Render.Post c = new net.yezon.theabyss.platform.client.event.ScreenEvent.Render.Post(e.getGuiGraphics(), e.getScreen());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.SelectMusicEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.SelectMusicEvent.class, e -> {
                net.yezon.theabyss.platform.client.event.SelectMusicEvent c = new net.yezon.theabyss.platform.client.event.SelectMusicEvent(e.getMusic());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setMusic(c.getMusic());
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.SubmitCustomGeometryEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent.class, e -> {
                net.yezon.theabyss.platform.client.event.SubmitCustomGeometryEvent c = new net.yezon.theabyss.platform.client.event.SubmitCustomGeometryEvent(e.getLevelRenderState(), e.getPoseStack(), e.getSubmitNodeCollector());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.TextureAtlasStitchedEvent.class)) {
            bus.addListener(priority(p), true, net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent.class, e -> {
                net.yezon.theabyss.platform.client.event.TextureAtlasStitchedEvent c = new net.yezon.theabyss.platform.client.event.TextureAtlasStitchedEvent(e.getAtlas());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.ViewportEvent.ComputeCameraAngles.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.ViewportEvent.ComputeCameraAngles.class, e -> {
                net.yezon.theabyss.platform.client.event.ViewportEvent.ComputeCameraAngles c = new net.yezon.theabyss.platform.client.event.ViewportEvent.ComputeCameraAngles(e.getPartialTick(), e.getPitch(), e.getRoll(), e.getYaw());
                AbyssEventBus.post(p, c);
                e.setPitch(c.getPitch());
                e.setRoll(c.getRoll());
                e.setYaw(c.getYaw());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.ViewportEvent.ComputeFogColor.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.ViewportEvent.ComputeFogColor.class, e -> {
                net.yezon.theabyss.platform.client.event.ViewportEvent.ComputeFogColor c = new net.yezon.theabyss.platform.client.event.ViewportEvent.ComputeFogColor(e.getBlue(), e.getGreen(), e.getRed());
                AbyssEventBus.post(p, c);
                e.setBlue(c.getBlue());
                e.setGreen(c.getGreen());
                e.setRed(c.getRed());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.ViewportEvent.ComputeFov.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.ViewportEvent.ComputeFov.class, e -> {
                net.yezon.theabyss.platform.client.event.ViewportEvent.ComputeFov c = new net.yezon.theabyss.platform.client.event.ViewportEvent.ComputeFov(e.getCamera(), e.getFOV(), e.getPartialTick());
                AbyssEventBus.post(p, c);
                e.setFOV(c.getFOV());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.ViewportEvent.RenderFog.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.ViewportEvent.RenderFog.class, e -> {
                net.yezon.theabyss.platform.client.event.ViewportEvent.RenderFog c = new net.yezon.theabyss.platform.client.event.ViewportEvent.RenderFog(e.getEnvironment(), e.getFarPlaneDistance(), e.getNearPlaneDistance());
                AbyssEventBus.post(p, c);
                e.setFarPlaneDistance(c.getFarPlaneDistance());
                e.setNearPlaneDistance(c.getNearPlaneDistance());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.sound.PlaySoundEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.sound.PlaySoundEvent.class, e -> {
                net.yezon.theabyss.platform.client.event.sound.PlaySoundEvent c = new net.yezon.theabyss.platform.client.event.sound.PlaySoundEvent(e.getOriginalSound(), e.getSound());
                AbyssEventBus.post(p, c);
                e.setSound(c.getSound());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.sound.PlaySoundSourceEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.sound.PlaySoundSourceEvent.class, e -> {
                net.yezon.theabyss.platform.client.event.sound.PlaySoundSourceEvent c = new net.yezon.theabyss.platform.client.event.sound.PlaySoundSourceEvent(e.getChannel(), e.getSound());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.sound.PlayStreamingSourceEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.sound.PlayStreamingSourceEvent.class, e -> {
                net.yezon.theabyss.platform.client.event.sound.PlayStreamingSourceEvent c = new net.yezon.theabyss.platform.client.event.sound.PlayStreamingSourceEvent(e.getChannel(), e.getSound());
                AbyssEventBus.post(p, c);
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.sound.SoundEngineLoadEvent.class)) {
            bus.addListener(priority(p), true, net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent.class, e -> {
                net.yezon.theabyss.platform.client.event.sound.SoundEngineLoadEvent c = new net.yezon.theabyss.platform.client.event.sound.SoundEngineLoadEvent();
                AbyssEventBus.post(p, c);
            });
        }
    }
}
