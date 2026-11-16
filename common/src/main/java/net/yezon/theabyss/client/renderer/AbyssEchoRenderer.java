package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.yezon.theabyss.entity.AbyssEchoEntity;
import org.jspecify.annotations.Nullable;

public class AbyssEchoRenderer extends HumanoidMobRenderer<AbyssEchoEntity, AbyssEchoRenderer.EchoRenderState, HumanoidModel<AbyssEchoRenderer.EchoRenderState>> {
    private static final int TINT = ARGB.color(190, 160, 225, 255);
    private final HumanoidModel<EchoRenderState> wide;
    private final HumanoidModel<EchoRenderState> slim;

    public static class EchoRenderState extends HumanoidRenderState {
        Identifier skin = DefaultPlayerSkin.getDefaultTexture();
        boolean slimArms;
    }

    public AbyssEchoRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.0F);
        this.wide = this.model;
        this.slim = new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM));
    }

    @Override
    public EchoRenderState createRenderState() {
        return new EchoRenderState();
    }

    @Override
    public void extractRenderState(AbyssEchoEntity entity, EchoRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        PlayerSkin skin = skinOf(entity.ownerId());
        state.skin = skin.body().texturePath();
        state.slimArms = skin.model() == PlayerModelType.SLIM;
    }

    private static PlayerSkin skinOf(String ownerId) {
        try {
            UUID uuid = UUID.fromString(ownerId);
            var connection = Minecraft.getInstance().getConnection();
            PlayerInfo info = connection != null ? connection.getPlayerInfo(uuid) : null;
            return info != null ? info.getSkin() : DefaultPlayerSkin.get(uuid);
        } catch (IllegalArgumentException noOwnerYet) {
            return DefaultPlayerSkin.getDefaultSkin();
        }
    }

    @Override
    public void submit(EchoRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        this.model = state.slimArms ? this.slim : this.wide;
        super.submit(state, poseStack, collector, camera);
    }

    @Override
    public Identifier getTextureLocation(EchoRenderState state) {
        return state.skin;
    }

    @Override
    protected @Nullable RenderType getRenderType(EchoRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        return RenderTypes.entityTranslucent(state.skin);
    }

    @Override
    protected int getModelTint(EchoRenderState state) {
        return TINT;
    }
}
