package net.yezon.theabyss.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.yezon.theabyss.neoforge.mixin.client.BlockColorsAccessor;
import net.yezon.theabyss.platform.client.event.CustomizeGuiOverlayEvent;
import net.yezon.theabyss.platform.client.event.ModelEvent;
import net.yezon.theabyss.platform.client.event.MovementInputUpdateEvent;
import net.yezon.theabyss.platform.client.event.RecipesReceivedEvent;
import net.yezon.theabyss.platform.client.event.RegisterColorHandlersEvent;
import net.yezon.theabyss.platform.client.event.RegisterDimensionSpecialEffectsEvent;
import net.yezon.theabyss.platform.client.event.RegisterItemPropertiesEvent;
import net.yezon.theabyss.platform.client.event.RegisterItemRenderersEvent;
import net.yezon.theabyss.platform.client.event.RegisterShadersEvent;
import net.yezon.theabyss.platform.client.event.RenderGuiEvent;
import net.yezon.theabyss.platform.client.event.RenderGuiLayerEvent;
import net.yezon.theabyss.platform.client.event.RenderLevelStageEvent;
import net.yezon.theabyss.platform.client.event.RenderLivingEvent;
import net.yezon.theabyss.platform.client.event.RenderPlayerEvent;
import net.yezon.theabyss.platform.client.event.ScreenEvent;
import net.yezon.theabyss.platform.client.event.ViewportEvent;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.event.EventPriority;
import org.jspecify.annotations.Nullable;

public final class NeoForgeClientEventBridge {
    private NeoForgeClientEventBridge() {
    }

    private static net.neoforged.bus.api.EventPriority priority(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> net.neoforged.bus.api.EventPriority.HIGHEST;
            case HIGH -> net.neoforged.bus.api.EventPriority.HIGH;
            case NORMAL -> net.neoforged.bus.api.EventPriority.NORMAL;
            case LOW -> net.neoforged.bus.api.EventPriority.LOW;
            case LOWEST -> net.neoforged.bus.api.EventPriority.LOWEST;
        };
    }

    public static void register(IEventBus modBus) {
        registerViewport();
        registerEntityRendering();
        registerGui();
        registerLevel();
        registerRegistration(modBus);
    }

    private static void registerViewport() {
        for (EventPriority p : AbyssEventBus.priorities(ViewportEvent.ComputeCameraAngles.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.ViewportEvent.ComputeCameraAngles.class, e -> {
                ViewportEvent.ComputeCameraAngles c = AbyssEventBus.post(p, new ViewportEvent.ComputeCameraAngles(e.getCamera(), (float)e.getPartialTick(),
                    e.getYaw(), e.getPitch(), e.getRoll()));
                e.setYaw(c.getYaw());
                e.setPitch(c.getPitch());
                e.setRoll(c.getRoll());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(ViewportEvent.ComputeFov.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.ViewportEvent.ComputeFov.class, e -> {
                float fov = (float)e.getFOV();
                ViewportEvent.ComputeFov c = AbyssEventBus.post(p, new ViewportEvent.ComputeFov(e.getCamera(), (float)e.getPartialTick(), fov, e.usedConfiguredFov()));
                if (c.getFOV() != fov) {
                    e.setFOV(c.getFOV());
                }
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(ViewportEvent.ComputeFogColor.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.ViewportEvent.ComputeFogColor.class, e -> {
                ViewportEvent.ComputeFogColor c = AbyssEventBus.post(p, new ViewportEvent.ComputeFogColor(e.getCamera(), (float)e.getPartialTick(),
                    e.getRed(), e.getGreen(), e.getBlue()));
                e.setRed(c.getRed());
                e.setGreen(c.getGreen());
                e.setBlue(c.getBlue());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(ViewportEvent.RenderFog.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.ViewportEvent.RenderFog.class, e -> {
                ViewportEvent.RenderFog c = AbyssEventBus.post(p, new ViewportEvent.RenderFog(e.getMode(), e.getType(), e.getCamera(), (float)e.getPartialTick(),
                    e.getNearPlaneDistance(), e.getFarPlaneDistance(), e.getFogShape()));
                if (c.isChanged()) {
                    e.setNearPlaneDistance(c.getNearPlaneDistance());
                    e.setFarPlaneDistance(c.getFarPlaneDistance());
                    e.setFogShape(c.getFogShape());
                    e.setCanceled(true);
                }
            });
        }
    }

    private static void registerEntityRendering() {
        for (EventPriority p : AbyssEventBus.priorities(RenderLivingEvent.Pre.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderLivingEvent.Pre.class, e -> {
                RenderLivingEvent.Pre c = new RenderLivingEvent.Pre(e.getEntity(), e.getRenderer(), e.getPartialTick(), e.getPoseStack(),
                    e.getMultiBufferSource(), e.getPackedLight());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(RenderLivingEvent.Post.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderLivingEvent.Post.class, e -> AbyssEventBus.post(p,
                new RenderLivingEvent.Post(e.getEntity(), e.getRenderer(), e.getPartialTick(), e.getPoseStack(), e.getMultiBufferSource(), e.getPackedLight())));
        }
        for (EventPriority p : AbyssEventBus.priorities(RenderPlayerEvent.Pre.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderPlayerEvent.Pre.class, e -> {
                RenderPlayerEvent.Pre c = new RenderPlayerEvent.Pre(e.getEntity(), e.getRenderer(), e.getPartialTick(), e.getPoseStack(),
                    e.getMultiBufferSource(), e.getPackedLight());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(RenderPlayerEvent.Post.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderPlayerEvent.Post.class, e -> AbyssEventBus.post(p,
                new RenderPlayerEvent.Post(e.getEntity(), e.getRenderer(), e.getPartialTick(), e.getPoseStack(), e.getMultiBufferSource(), e.getPackedLight())));
        }
    }

    private static void registerGui() {
        for (EventPriority p : AbyssEventBus.priorities(CustomizeGuiOverlayEvent.BossEventProgress.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent.BossEventProgress.class, e -> {
                CustomizeGuiOverlayEvent.BossEventProgress c = new CustomizeGuiOverlayEvent.BossEventProgress(e.getGuiGraphics(), e.getPartialTick(),
                    e.getBossEvent(), e.getX(), e.getY(), e.getIncrement());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setIncrement(c.getIncrement());
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(RenderGuiEvent.Pre.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderGuiEvent.Pre.class, e -> {
                RenderGuiEvent.Pre c = new RenderGuiEvent.Pre(e.getGuiGraphics(), e.getPartialTick());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(RenderGuiEvent.Post.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderGuiEvent.Post.class,
                e -> AbyssEventBus.post(p, new RenderGuiEvent.Post(e.getGuiGraphics(), e.getPartialTick())));
        }
        for (EventPriority p : AbyssEventBus.priorities(RenderGuiLayerEvent.Pre.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre.class, e -> {
                RenderGuiLayerEvent.Pre c = new RenderGuiLayerEvent.Pre(e.getGuiGraphics(), e.getPartialTick(), e.getName());
                c.setCanceled(e.isCanceled());
                AbyssEventBus.post(p, c);
                e.setCanceled(c.isCanceled());
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(RenderGuiLayerEvent.Post.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Post.class,
                e -> AbyssEventBus.post(p, new RenderGuiLayerEvent.Post(e.getGuiGraphics(), e.getPartialTick(), e.getName())));
        }
        for (EventPriority p : AbyssEventBus.priorities(ScreenEvent.Render.Post.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.ScreenEvent.Render.Post.class,
                e -> AbyssEventBus.post(p, new ScreenEvent.Render.Post(e.getScreen(), e.getGuiGraphics(), e.getMouseX(), e.getMouseY(), e.getPartialTick())));
        }
    }

    private static void registerLevel() {
        for (EventPriority p : AbyssEventBus.priorities(RenderLevelStageEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RenderLevelStageEvent.class, e -> {
                RenderLevelStageEvent.Stage stage = RenderLevelStageEvent.Stage.byName(e.getStage().toString());
                if (stage == null) {
                    return;
                }
                AbyssEventBus.post(p, new RenderLevelStageEvent(stage, e.getLevelRenderer(), e.getPoseStack(), e.getModelViewMatrix(),
                    e.getProjectionMatrix(), e.getRenderTick(), e.getPartialTick(), e.getCamera(), e.getFrustum()));
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(MovementInputUpdateEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.MovementInputUpdateEvent.class,
                e -> AbyssEventBus.post(p, new MovementInputUpdateEvent(e.getEntity(), e.getInput())));
        }
        for (EventPriority p : AbyssEventBus.priorities(RecipesReceivedEvent.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.client.event.RecipesUpdatedEvent.class,
                e -> AbyssEventBus.post(p, new RecipesReceivedEvent(e.getRecipeManager())));
        }
    }

    private static void registerRegistration(IEventBus modBus) {
        for (EventPriority p : AbyssEventBus.priorities(ModelEvent.ModifyBakingResult.class)) {
            modBus.addListener(priority(p), true, net.neoforged.neoforge.client.event.ModelEvent.ModifyBakingResult.class,
                e -> AbyssEventBus.post(p, new ModelEvent.ModifyBakingResult(e.getModels(), e.getModelBakery())));
        }
        for (EventPriority p : AbyssEventBus.priorities(RegisterShadersEvent.class)) {
            modBus.addListener(priority(p), true, net.neoforged.neoforge.client.event.RegisterShadersEvent.class, e -> AbyssEventBus.post(p, new RegisterShadersEvent() {
                @Override
                public void registerShader(ResourceLocation id, VertexFormat format, Consumer<ShaderInstance> onLoad) {
                    try {
                        e.registerShader(new ShaderInstance(e.getResourceProvider(), id, format), onLoad);
                    } catch (IOException exception) {
                        throw new UncheckedIOException("Failed to load shader " + id, exception);
                    }
                }
            }));
        }
        for (EventPriority p : AbyssEventBus.priorities(RegisterDimensionSpecialEffectsEvent.class)) {
            modBus.addListener(priority(p), true, net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent.class,
                e -> AbyssEventBus.post(p, new RegisterDimensionSpecialEffectsEvent() {
                    @Override
                    public void register(ResourceLocation effectsId, DimensionSpecialEffects effects) {
                        e.register(effectsId, effects);
                    }
                }));
        }
        for (EventPriority p : AbyssEventBus.priorities(RegisterColorHandlersEvent.Block.class)) {
            modBus.addListener(priority(p), true, net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Block.class,
                e -> AbyssEventBus.post(p, new RegisterColorHandlersEvent.Block() {
                    @Override
                    public BlockColors getBlockColors() {
                        return e.getBlockColors();
                    }

                    @Override
                    public void register(BlockColor color, net.minecraft.world.level.block.Block... blocks) {
                        e.register(color, blocks);
                    }

                    @Override
                    public @Nullable BlockColor getColor(net.minecraft.world.level.block.Block block) {
                        return ((BlockColorsAccessor)e.getBlockColors()).theabyss$blockColors().get(block);
                    }
                }));
        }
        for (EventPriority p : AbyssEventBus.priorities(RegisterColorHandlersEvent.Item.class)) {
            modBus.addListener(priority(p), true, net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item.class,
                e -> AbyssEventBus.post(p, new RegisterColorHandlersEvent.Item() {
                    @Override
                    public ItemColors getItemColors() {
                        return e.getItemColors();
                    }

                    @Override
                    public BlockColors getBlockColors() {
                        return e.getBlockColors();
                    }

                    @Override
                    public void register(ItemColor color, ItemLike... items) {
                        e.register(color, items);
                    }
                }));
        }
        if (AbyssEventBus.hasListeners(RegisterItemPropertiesEvent.class)) {
            modBus.addListener(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent.class, e -> e.enqueueWork(() -> AbyssEventBus.post(new RegisterItemPropertiesEvent() {
                @Override
                public void register(Item item, ResourceLocation property, ClampedItemPropertyFunction function) {
                    ItemProperties.register(item, property, (ItemPropertyFunction)function);
                }

                @Override
                public void registerGeneric(ResourceLocation property, ClampedItemPropertyFunction function) {
                    ItemProperties.registerGeneric(property, (ItemPropertyFunction)function);
                }
            })));
        }
        if (AbyssEventBus.hasListeners(RegisterItemRenderersEvent.class)) {
            modBus.addListener(RegisterClientExtensionsEvent.class, e -> AbyssEventBus.post(new RegisterItemRenderersEvent() {
                @Override
                public void register(ItemRenderer renderer, ItemLike... items) {
                    List<Item> list = new ArrayList<>(items.length);
                    for (ItemLike like : items) {
                        list.add(like.asItem());
                    }
                    e.registerItem(new ItemRendererExtensions(renderer), list.toArray(new Item[0]));
                }
            }));
        }
    }

    private static final class ItemRendererExtensions implements IClientItemExtensions {
        private final RegisterItemRenderersEvent.ItemRenderer renderer;
        private @Nullable BlockEntityWithoutLevelRenderer bewlr;

        private ItemRendererExtensions(RegisterItemRenderersEvent.ItemRenderer renderer) {
            this.renderer = renderer;
        }

        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            BlockEntityWithoutLevelRenderer current = this.bewlr;
            if (current == null) {
                Minecraft mc = Minecraft.getInstance();
                RegisterItemRenderersEvent.ItemRenderer target = this.renderer;
                current = new BlockEntityWithoutLevelRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels()) {
                    @Override
                    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                        int packedOverlay) {
                        target.render(stack, context, poseStack, buffers, packedLight, packedOverlay);
                    }
                };
                this.bewlr = current;
            }
            return current;
        }
    }
}
