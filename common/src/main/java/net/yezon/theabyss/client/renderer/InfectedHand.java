package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.EntityRenderersEvent;
import net.yezon.theabyss.platform.client.event.RenderArmEvent;
import net.yezon.theabyss.platform.client.event.RenderHandEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.mixin.client.ItemInHandRendererArmInvoker;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class InfectedHand {
    private static final float INFLATE = 0.27F;
    private static final int FULL_BRIGHT = 15728880;
    private static final Map<PlayerSkin.Model, ModelPart> ARM_BOX = new EnumMap<>(PlayerSkin.Model.class);

    private InfectedHand() {
    }

    private static ResourceLocation texture(PlayerSkin.Model type, String suffix) {
        return ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID,
            "textures/entity/infected_hand_" + (type == PlayerSkin.Model.SLIM ? "slim" : "wide") + suffix + ".png");
    }

    private static ModelPart armBox(PlayerSkin.Model type) {
        return ARM_BOX.computeIfAbsent(type, t -> {
            boolean slim = t == PlayerSkin.Model.SLIM;
            MeshDefinition mesh = new MeshDefinition();
            mesh.getRoot().addOrReplaceChild("arm", CubeListBuilder.create().texOffs(0, 0)
                .addBox(slim ? -2.0F : -3.0F, -2.0F, -2.0F, slim ? 3.0F : 4.0F, 12.0F, 4.0F, new CubeDeformation(INFLATE)), PartPose.ZERO);
            return LayerDefinition.create(mesh, 16, 16).bakeRoot();
        });
    }

    public static boolean holds(Player player) {
        return player.getMainHandItem().is(TheabyssModItems.INFECTED_GAUNTLET.get()) || player.getOffhandItem().is(TheabyssModItems.INFECTED_GAUNTLET.get())
            || net.yezon.theabyss.client.fx.OvergrowthClient.overgrown(player.getId());
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model type : event.getSkins()) {
            EntityRenderer<? extends Player> renderer = event.getSkin(type);
            if (renderer instanceof PlayerRenderer player) {
                ((net.yezon.theabyss.mixin.accessor.client.LivingEntityRendererAccessor)player).theabyss$addLayer(new Layer(player, type));
            }
        }
    }

    private static final class Layer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        private final PlayerSkin.Model type;

        Layer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer, PlayerSkin.Model type) {
            super(renderer);
            this.type = type;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                           float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (!holds(player) || player.isInvisible()) {
                return;
            }
            PlayerModel<AbstractClientPlayer> parent = this.getParentModel();
            ModelPart box = armBox(this.type);
            poseStack.pushPose();
            parent.rightArm.translateAndRotate(poseStack);
            box.render(poseStack, buffers.getBuffer(RenderType.entityTranslucent(texture(this.type, ""))), light, OverlayTexture.NO_OVERLAY);
            box.render(poseStack, buffers.getBuffer(AbyssRenderTypes.eyes(texture(this.type, "_glow"))), light, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }

    private static void renderRightHand(PlayerRenderer renderer, PoseStack poseStack, MultiBufferSource buffers, int light, ResourceLocation skin, boolean sleeve) {
        PlayerModel<AbstractClientPlayer> model = renderer.getModel();
        model.rightArm.resetPose();
        model.rightArm.visible = true;
        model.leftSleeve.visible = sleeve;
        model.rightSleeve.visible = sleeve;
        model.leftArm.zRot = -0.1F;
        model.rightArm.zRot = 0.1F;
        model.rightSleeve.copyFrom(model.rightArm);
        RenderType type = RenderType.entityTranslucent(skin);
        model.rightArm.render(poseStack, buffers.getBuffer(type), light, OverlayTexture.NO_OVERLAY);
        model.rightSleeve.render(poseStack, buffers.getBuffer(type), light, OverlayTexture.NO_OVERLAY);
    }

    private static void infectedHand(PlayerRenderer renderer, PoseStack poseStack, MultiBufferSource buffers, int light, ResourceLocation skin, boolean sleeve,
                                     PlayerSkin.Model type) {
        renderRightHand(renderer, poseStack, buffers, light, skin, sleeve);
        renderRightHand(renderer, poseStack, buffers, light, texture(type, "_skin"), sleeve);
        renderRightHand(renderer, poseStack, buffers, FULL_BRIGHT, texture(type, "_skin_glow"), sleeve);
    }

    private static @Nullable PlayerRenderer rendererOf(AbstractClientPlayer player) {
        return Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer ? renderer : null;
    }

    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        AbstractClientPlayer player = event.getPlayer();
        if (event.getArm() != HumanoidArm.RIGHT || player == null || !holds(player)) {
            return;
        }
        PlayerRenderer renderer = rendererOf(player);
        if (renderer == null) {
            return;
        }
        event.setCanceled(true);
        PlayerSkin skin = player.getSkin();
        infectedHand(renderer, event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), skin.texture(),
            player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE), skin.model());
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !event.getItemStack().is(TheabyssModItems.INFECTED_GAUNTLET.get())) {
            return;
        }
        AbstractClientPlayer player = Minecraft.getInstance().player;
        if (player == null || player.getMainArm() != HumanoidArm.RIGHT) {
            return;
        }
        event.setCanceled(true);
        if (player.isInvisible()) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        ((ItemInHandRendererArmInvoker)Minecraft.getInstance().gameRenderer.itemInHandRenderer).theabyss$renderPlayerArm(poseStack,
            event.getMultiBufferSource(), event.getPackedLight(), event.getEquipProgress(), event.getSwingProgress(), HumanoidArm.RIGHT);
        poseStack.popPose();
    }
}
