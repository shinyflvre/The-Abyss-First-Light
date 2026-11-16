package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.entity.SilentEchoEntity;
import org.jspecify.annotations.Nullable;

public class SilentEchoRenderer extends HumanoidMobRenderer<SilentEchoEntity, SilentEchoRenderer.EchoState, HumanoidModel<SilentEchoRenderer.EchoState>> {
    private static final int TINT = ARGB.color(240, 238, 236, 255);
    private static final int VESSEL_TINT = ARGB.color(215, 176, 170, 196);
    private static final float VESSEL_BOW = 32.0F;
    private final HumanoidModel<EchoState> wide;
    private final HumanoidModel<EchoState> slim;

    public static class EchoState extends HumanoidRenderState {
        Identifier skin = DefaultPlayerSkin.getDefaultTexture();
        boolean slimArms;
        boolean vessel;
    }

    public SilentEchoRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.wide = this.model;
        this.slim = new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM));
        this.addLayer(new HumanoidArmorLayer<>(this,
            ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), part -> new HumanoidModel<EchoState>(part)),
            context.getEquipmentRenderer()));
    }

    @Override
    public EchoState createRenderState() {
        return new EchoState();
    }

    @Override
    public void extractRenderState(SilentEchoEntity entity, EchoState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        PlayerSkin skin = skinOf(entity.ownerId());
        state.skin = skin.body().texturePath();
        state.slimArms = skin.model() == PlayerModelType.SLIM;
        state.vessel = entity.isVessel();
        if (state.vessel) {
            state.xRot = VESSEL_BOW;
        }
    }

    private static PlayerSkin skinOf(@Nullable UUID owner) {
        if (owner == null) {
            return DefaultPlayerSkin.getDefaultSkin();
        }
        var connection = Minecraft.getInstance().getConnection();
        PlayerInfo info = connection != null ? connection.getPlayerInfo(owner) : null;
        return info != null ? info.getSkin() : DefaultPlayerSkin.get(owner);
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(SilentEchoEntity mob, HumanoidArm arm) {
        ItemStack held = mob.getItemHeldByArm(arm);
        if (held.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        }
        if (mob.isAggressive() && held.getItem() instanceof BowItem) {
            return HumanoidModel.ArmPose.BOW_AND_ARROW;
        }
        if (mob.isAggressive() && held.getItem() instanceof CrossbowItem) {
            return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        }
        return HumanoidModel.ArmPose.ITEM;
    }

    @Override
    public void submit(EchoState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        this.model = state.slimArms ? this.slim : this.wide;
        super.submit(state, poseStack, collector, camera);
    }

    @Override
    public Identifier getTextureLocation(EchoState state) {
        return state.skin;
    }

    @Override
    protected @Nullable RenderType getRenderType(EchoState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        return RenderTypes.entityTranslucent(state.skin);
    }

    @Override
    protected int getModelTint(EchoState state) {
        return state.vessel ? VESSEL_TINT : TINT;
    }
}
