package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.fx.FearClient;
import net.yezon.theabyss.entity.FearShadeEntity;
import org.jspecify.annotations.Nullable;

public class FearShadeRenderer extends HumanoidMobRenderer<FearShadeEntity, FearShadeRenderer.ShadeState, HumanoidModel<FearShadeRenderer.ShadeState>> {
    private static final Identifier EYES = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "textures/entity/fear_shade_eyes.png");
    private static final int TINT = ARGB.color(250, 12, 14, 20);
    private static final RandomSource RANDOM = RandomSource.create();
    private final HumanoidModel<ShadeState> wide;
    private final HumanoidModel<ShadeState> slim;

    public static class ShadeState extends HumanoidRenderState {
        Identifier skin = DefaultPlayerSkin.getDefaultTexture();
        boolean slimArms;
    }

    public FearShadeRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.0F);
        this.wide = this.model;
        this.slim = new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM));
        this.addLayer(new EyesLayer(this));
    }

    @Override
    public ShadeState createRenderState() {
        return new ShadeState();
    }

    @Override
    public void extractRenderState(FearShadeEntity entity, ShadeState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        PlayerSkin skin = localSkin();
        state.skin = skin.body().texturePath();
        state.slimArms = skin.model() == PlayerModelType.SLIM;
    }

    private static PlayerSkin localSkin() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return DefaultPlayerSkin.getDefaultSkin();
        }
        var connection = mc.getConnection();
        PlayerInfo info = connection != null ? connection.getPlayerInfo(mc.player.getUUID()) : null;
        return info != null ? info.getSkin() : DefaultPlayerSkin.get(mc.player.getUUID());
    }

    @Override
    public void submit(ShadeState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float jitter = FearClient.shadeJitter();
        if (jitter > 0.6F && RANDOM.nextFloat() < 0.35F) {
            return;
        }
        this.model = state.slimArms ? this.slim : this.wide;
        poseStack.pushPose();
        if (jitter > 0.0F) {
            poseStack.translate((RANDOM.nextFloat() - 0.5F) * 0.18F * jitter, 0.0F, (RANDOM.nextFloat() - 0.5F) * 0.18F * jitter);
        }
        super.submit(state, poseStack, collector, camera);
        poseStack.popPose();
    }

    @Override
    public Identifier getTextureLocation(ShadeState state) {
        return state.skin;
    }

    @Override
    protected @Nullable RenderType getRenderType(ShadeState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        return RenderTypes.entityTranslucent(state.skin);
    }

    @Override
    protected int getModelTint(ShadeState state) {
        return TINT;
    }

    private static final class EyesLayer extends RenderLayer<ShadeState, HumanoidModel<ShadeState>> {
        EyesLayer(RenderLayerParent<ShadeState, HumanoidModel<ShadeState>> parent) {
            super(parent);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, ShadeState state, float yRot, float xRot) {
            collector.order(1).submitModel(this.getParentModel(), state, poseStack, RenderTypes.eyes(EYES), light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        }
    }
}
