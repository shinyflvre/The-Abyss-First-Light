package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.yezon.theabyss.entity.NightbladeBossEntity;
import net.yezon.theabyss.entity.layer.GlowGeoLayer;
import net.yezon.theabyss.entity.model.NightbladeModel;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class NightbladeBossRenderer extends GeoEntityRenderer<NightbladeBossEntity> {
    private static final ResourceLocation GLOW = ResourceLocation.parse("theabyss:textures/entity/nightblade_glowing_layer.png");
    private static final String RIGHT_HAND = "RightHandItem";
    private static final String LEFT_HAND = "LeftHandItem";
    private static final int FULL_BRIGHT = 0xF000F0;

    public NightbladeBossRenderer(Context context) {
        super(context, new NightbladeModel());
        this.shadowRadius = 0.7F;
        this.addRenderLayer(new HandItems(this));
        this.addRenderLayer(new GlowGeoLayer<>(this, GLOW));
    }

    private static final class HandItems extends BlockAndItemGeoLayer<NightbladeBossEntity> {
        HandItems(NightbladeBossRenderer renderer) {
            super(renderer);
        }

        private static @Nullable HumanoidArm arm(GeoBone bone) {
            return RIGHT_HAND.equals(bone.getName()) ? HumanoidArm.RIGHT : LEFT_HAND.equals(bone.getName()) ? HumanoidArm.LEFT : null;
        }

        private static HumanoidArm mainArm(NightbladeBossEntity animatable) {
            return animatable.getMainArm() == HumanoidArm.LEFT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        }

        @Override
        protected @Nullable ItemStack getStackForBone(GeoBone bone, NightbladeBossEntity animatable) {
            HumanoidArm arm = arm(bone);
            if (arm == null) {
                return null;
            }
            return animatable.getItemBySlot(arm == mainArm(animatable) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }

        @Override
        protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, NightbladeBossEntity animatable) {
            HumanoidArm arm = arm(bone);
            if (arm == null) {
                return ItemDisplayContext.NONE;
            }
            return arm == HumanoidArm.RIGHT ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        }

        @Override
        protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, NightbladeBossEntity animatable, MultiBufferSource bufferSource,
                                          float partialTick, int packedLight, int packedOverlay) {
            ItemDisplayContext context = this.getTransformTypeForStack(bone, stack, animatable);
            boolean leftHanded = animatable.getMainArm() == HumanoidArm.LEFT;
            boolean mainShield = animatable.getMainHandItem().getItem() instanceof ShieldItem;
            boolean offShield = animatable.getOffhandItem().getItem() instanceof ShieldItem;
            poseStack.pushPose();
            if (context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
                poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
                poseStack.translate(0.0F, 0.125F, -0.0625F);
                if (leftHanded ? offShield : mainShield) {
                    poseStack.translate(0.0, 0.125, -0.25);
                }
            } else if (context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
                poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
                poseStack.translate(0.0F, 0.125F, -0.0625F);
                if (leftHanded ? mainShield : offShield) {
                    poseStack.translate(0.0, 0.125, 0.25);
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                }
            }
            Minecraft.getInstance().getItemRenderer().renderStatic(animatable, stack, context, context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND, poseStack,
                bufferSource, animatable.level(), FULL_BRIGHT, OverlayTexture.NO_OVERLAY, animatable.getId());
            poseStack.popPose();
        }
    }
}
