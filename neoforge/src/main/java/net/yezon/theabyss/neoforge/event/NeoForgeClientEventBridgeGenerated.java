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
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.client.event.EntityRenderersEvent.AddLayers.class)) {
            bus.addListener(net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.client.event.EntityRenderersEvent.AddLayers() {
                @Override
                public java.util.Set<net.minecraft.client.resources.PlayerSkin.Model> getSkins() {
                    return e.getSkins();
                }
                @Override
                public <R extends net.minecraft.client.renderer.entity.EntityRenderer<? extends net.minecraft.world.entity.player.Player>> R getSkin(net.minecraft.client.resources.PlayerSkin.Model a0) {
                    return e.getSkin(a0);
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
                public <T extends net.minecraft.world.level.block.entity.BlockEntity> void registerBlockEntityRenderer(net.minecraft.world.level.block.entity.BlockEntityType<? extends T> a0, net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider<T> a1) {
                    e.registerBlockEntityRenderer(a0, a1);
                }
                @Override
                public <T extends net.minecraft.world.entity.Entity> void registerEntityRenderer(net.minecraft.world.entity.EntityType<? extends T> a0, net.minecraft.client.renderer.entity.EntityRendererProvider<T> a1) {
                    e.registerEntityRenderer(a0, a1);
                }
            }));
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
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.client.event.RegisterClientCommandsEvent.class)) {
            NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.client.event.RegisterClientCommandsEvent.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.client.event.RegisterClientCommandsEvent() {
                @Override
                public com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> getDispatcher() {
                    return e.getDispatcher();
                }
            }));
        }
        if (AbyssEventBus.hasListeners(net.yezon.theabyss.platform.client.event.RegisterKeyMappingsEvent.class)) {
            bus.addListener(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent.class, e -> AbyssEventBus.post(new net.yezon.theabyss.platform.client.event.RegisterKeyMappingsEvent() {
                @Override
                public void register(net.minecraft.client.KeyMapping a0) {
                    e.register(a0);
                }
            }));
        }
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.RenderArmEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderArmEvent.class, e -> {
                net.yezon.theabyss.platform.client.event.RenderArmEvent c = new net.yezon.theabyss.platform.client.event.RenderArmEvent(e.getPoseStack(), e.getMultiBufferSource(), e.getPackedLight(), e.getPlayer(), e.getArm());
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
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.RenderHandEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderHandEvent.class, e -> {
                net.yezon.theabyss.platform.client.event.RenderHandEvent c = new net.yezon.theabyss.platform.client.event.RenderHandEvent(e.getHand(), e.getPoseStack(), e.getMultiBufferSource(), e.getPackedLight(), e.getPartialTick(), e.getInterpolatedPitch(), e.getSwingProgress(), e.getEquipProgress(), e.getItemStack());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
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
        for (EventPriority p : AbyssEventBus.priorities(net.yezon.theabyss.platform.client.event.TextureAtlasStitchedEvent.class)) {
            bus.addListener(priority(p), true, net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent.class, e -> {
                net.yezon.theabyss.platform.client.event.TextureAtlasStitchedEvent c = new net.yezon.theabyss.platform.client.event.TextureAtlasStitchedEvent(e.getAtlas());
                AbyssEventBus.post(p, c);
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
