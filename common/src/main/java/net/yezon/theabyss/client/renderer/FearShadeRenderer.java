package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.compat.ARGB;
import net.minecraft.util.RandomSource;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.fx.FearClient;
import net.yezon.theabyss.entity.FearShadeEntity;

public class FearShadeRenderer extends HumanoidMobRenderer<FearShadeEntity, PlayerModel<FearShadeEntity>> {
    private static final ResourceLocation EYES = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "textures/entity/fear_shade_eyes.png");
    private static final int TINT = ARGB.color(250, 12, 14, 20);
    private static final RandomSource RANDOM = RandomSource.create();
    private final PlayerModel<FearShadeEntity> wide;
    private final PlayerModel<FearShadeEntity> slim;

    public FearShadeRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.0F);
        this.wide = this.model;
        this.slim = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        this.addLayer(new EyesLayer(this));
    }

    @Override
    public void render(FearShadeEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        float jitter = FearClient.shadeJitter();
        if (jitter > 0.6F && RANDOM.nextFloat() < 0.35F) {
            return;
        }
        EchoSkins skin = EchoSkins.local();
        this.model = skin.slim() ? this.slim : this.wide;
        poseStack.pushPose();
        if (jitter > 0.0F) {
            poseStack.translate((RANDOM.nextFloat() - 0.5F) * 0.18F * jitter, 0.0F, (RANDOM.nextFloat() - 0.5F) * 0.18F * jitter);
        }
        super.render(entity, entityYaw, partialTicks, poseStack, TintedBuffers.of(buffers, RenderType.entityTranslucent(skin.texture()), TINT), light);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(FearShadeEntity entity) {
        return EchoSkins.local().texture();
    }

    @Override
    protected RenderType getRenderType(FearShadeEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(this.getTextureLocation(entity));
    }

    private static final class EyesLayer extends RenderLayer<FearShadeEntity, PlayerModel<FearShadeEntity>> {
        EyesLayer(RenderLayerParent<FearShadeEntity, PlayerModel<FearShadeEntity>> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffers, int light, FearShadeEntity entity, float limbSwing, float limbSwingAmount,
                           float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            this.getParentModel().renderToBuffer(poseStack, buffers.getBuffer(AbyssRenderTypes.eyes(EYES)), light, OverlayTexture.NO_OVERLAY);
        }
    }
}
