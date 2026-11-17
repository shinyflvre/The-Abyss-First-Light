package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.compat.ARGB;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.entity.SilentEchoEntity;

public class SilentEchoRenderer extends HumanoidMobRenderer<SilentEchoEntity, SilentEchoRenderer.EchoModel> {
    private static final int TINT = ARGB.color(240, 238, 236, 255);
    private static final int VESSEL_TINT = ARGB.color(215, 176, 170, 196);
    private static final float VESSEL_BOW = 32.0F;
    private final EchoModel wide;
    private final EchoModel slim;

    public SilentEchoRenderer(EntityRendererProvider.Context context) {
        super(context, new EchoModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.wide = this.model;
        this.slim = new EchoModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<SilentEchoEntity>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
            new HumanoidModel<SilentEchoEntity>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
    }

    private static HumanoidModel.ArmPose getArmPose(SilentEchoEntity mob, HumanoidArm arm) {
        ItemStack held = arm == mob.getMainArm() ? mob.getMainHandItem() : mob.getOffhandItem();
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
    public void render(SilentEchoEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        EchoSkins skin = EchoSkins.of(entity.ownerId());
        this.model = skin.slim() ? this.slim : this.wide;
        int tint = entity.isVessel() ? VESSEL_TINT : TINT;
        super.render(entity, entityYaw, partialTicks, poseStack, TintedBuffers.of(buffers, RenderType.entityTranslucent(skin.texture()), tint), light);
    }

    @Override
    public ResourceLocation getTextureLocation(SilentEchoEntity entity) {
        return EchoSkins.of(entity.ownerId()).texture();
    }

    @Override
    protected RenderType getRenderType(SilentEchoEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(this.getTextureLocation(entity));
    }

    public static class EchoModel extends PlayerModel<SilentEchoEntity> {
        public EchoModel(ModelPart root, boolean slim) {
            super(root, slim);
        }

        @Override
        public void prepareMobModel(SilentEchoEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
            this.leftArmPose = getArmPose(entity, HumanoidArm.LEFT);
            this.rightArmPose = getArmPose(entity, HumanoidArm.RIGHT);
            super.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
        }

        @Override
        public void setupAnim(SilentEchoEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, entity.isVessel() ? VESSEL_BOW : headPitch);
        }
    }
}
