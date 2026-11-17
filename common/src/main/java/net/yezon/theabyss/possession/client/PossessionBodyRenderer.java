package net.yezon.theabyss.possession.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.possession.PossessionBodyEntity;

public class PossessionBodyRenderer extends EntityRenderer<PossessionBodyEntity> {
    private static final PlayerSkin DEFAULT_SKIN = DefaultPlayerSkin.get(Util.NIL_UUID);
    private final Part wide;
    private final Part slim;

    public PossessionBodyRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.wide = new Part(context, false);
        this.slim = new Part(context, true);
        this.shadowRadius = 0.5F;
    }

    public static PlayerSkin skin(PossessionBodyEntity body) {
        try {
            PlayerSkin skin = Minecraft.getInstance().getSkinManager().getOrLoad(body.getProfile().gameProfile()).getNow(null);
            return skin != null ? skin : DEFAULT_SKIN;
        } catch (RuntimeException e) {
            return DEFAULT_SKIN;
        }
    }

    private Part part(PossessionBodyEntity body) {
        return skin(body).model() == PlayerSkin.Model.SLIM ? this.slim : this.wide;
    }

    @Override
    public void render(PossessionBodyEntity body, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        this.part(body).render(body, yaw, partialTick, poseStack, buffers, light);
    }

    @Override
    public Vec3 getRenderOffset(PossessionBodyEntity body, float partialTick) {
        return this.part(body).getRenderOffset(body, partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(PossessionBodyEntity body) {
        return skin(body).texture();
    }

    @Override
    protected float getShadowRadius(PossessionBodyEntity body) {
        return this.shadowRadius * body.getScale();
    }

    static final class Part extends LivingEntityRenderer<PossessionBodyEntity, PlayerModel<PossessionBodyEntity>> {
        Part(EntityRendererProvider.Context context, boolean slim) {
            super(context, new PlayerModel<>(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim), 0.5F);
            this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidArmorModel<>(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel<>(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()
            ));
            this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
            this.addLayer(new ArrowLayer<>(context, this));
            this.addLayer(new Cape(this));
            this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
            this.addLayer(new Wings(this, context.getModelSet()));
            this.addLayer(new SpinAttackEffectLayer<>(this, context.getModelSet()));
            this.addLayer(new BeeStingerLayer<>(this));
        }

        @Override
        public void render(PossessionBodyEntity body, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
            this.setModelProperties(body);
            super.render(body, yaw, partialTick, poseStack, buffers, light);
        }

        @Override
        public Vec3 getRenderOffset(PossessionBodyEntity body, float partialTick) {
            return body.isCrouching() ? new Vec3(0.0, body.getScale() * -2.0F / 16.0, 0.0) : super.getRenderOffset(body, partialTick);
        }

        private void setModelProperties(PossessionBodyEntity body) {
            PlayerModel<PossessionBodyEntity> model = this.getModel();
            if (body.isSpectator()) {
                model.setAllVisible(false);
                model.head.visible = true;
                model.hat.visible = true;
                return;
            }
            model.setAllVisible(true);
            model.hat.visible = body.isModelPartShown(PlayerModelPart.HAT);
            model.jacket.visible = body.isModelPartShown(PlayerModelPart.JACKET);
            model.leftPants.visible = body.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
            model.rightPants.visible = body.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
            model.leftSleeve.visible = body.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
            model.rightSleeve.visible = body.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
            model.crouching = body.isCrouching();
            HumanoidModel.ArmPose main = armPose(body, InteractionHand.MAIN_HAND);
            HumanoidModel.ArmPose off = armPose(body, InteractionHand.OFF_HAND);
            if (main.isTwoHanded()) {
                off = body.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
            }
            if (body.getMainArm() == HumanoidArm.RIGHT) {
                model.rightArmPose = main;
                model.leftArmPose = off;
            } else {
                model.rightArmPose = off;
                model.leftArmPose = main;
            }
        }

        private static HumanoidModel.ArmPose armPose(PossessionBodyEntity body, InteractionHand hand) {
            ItemStack stack = body.getItemInHand(hand);
            if (stack.isEmpty()) {
                return HumanoidModel.ArmPose.EMPTY;
            }
            if (body.getUsedItemHand() == hand && body.getUseItemRemainingTicks() > 0) {
                UseAnim anim = stack.getUseAnimation();
                if (anim == UseAnim.BLOCK) {
                    return HumanoidModel.ArmPose.BLOCK;
                }
                if (anim == UseAnim.BOW) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }
                if (anim == UseAnim.SPEAR) {
                    return HumanoidModel.ArmPose.THROW_SPEAR;
                }
                if (anim == UseAnim.CROSSBOW) {
                    return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                }
                if (anim == UseAnim.SPYGLASS) {
                    return HumanoidModel.ArmPose.SPYGLASS;
                }
                if (anim == UseAnim.TOOT_HORN) {
                    return HumanoidModel.ArmPose.TOOT_HORN;
                }
                if (anim == UseAnim.BRUSH) {
                    return HumanoidModel.ArmPose.BRUSH;
                }
            } else if (!body.swinging && stack.is(Items.CROSSBOW) && CrossbowItem.isCharged(stack)) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }
            return HumanoidModel.ArmPose.ITEM;
        }

        @Override
        public ResourceLocation getTextureLocation(PossessionBodyEntity body) {
            return skin(body).texture();
        }

        @Override
        protected void scale(PossessionBodyEntity body, PoseStack poseStack, float partialTick) {
            poseStack.scale(0.9375F, 0.9375F, 0.9375F);
        }

        @Override
        protected boolean shouldShowName(PossessionBodyEntity body) {
            return super.shouldShowName(body)
                && (body.shouldShowName() || body.hasCustomName() && body == this.entityRenderDispatcher.crosshairPickEntity);
        }

        @Override
        protected void setupRotations(PossessionBodyEntity body, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
            float swim = body.getSwimAmount(partialTick);
            float xRot = body.getViewXRot(partialTick);
            if (body.isFallFlying()) {
                super.setupRotations(body, poseStack, bob, yBodyRot, partialTick, scale);
                float ticks = body.getFallFlyingTicks() + partialTick;
                float amount = Mth.clamp(ticks * ticks / 100.0F, 0.0F, 1.0F);
                if (!body.isAutoSpinAttack()) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(amount * (-90.0F - xRot)));
                }
                Vec3 look = body.getViewVector(partialTick);
                Vec3 movement = body.getDeltaMovementLerped(partialTick);
                if (movement.horizontalDistanceSqr() > 1.0E-5F && look.horizontalDistanceSqr() > 1.0E-5F) {
                    Vec3 moveFlat = new Vec3(movement.x, 0.0, movement.z).normalize();
                    Vec3 lookFlat = new Vec3(look.x, 0.0, look.z).normalize();
                    double dot = moveFlat.dot(lookFlat);
                    double sign = movement.x * look.z - movement.z * look.x;
                    poseStack.mulPose(Axis.YP.rotation((float)(Math.signum(sign) * Math.acos(Math.min(1.0, Math.abs(dot))))));
                }
            } else if (swim > 0.0F) {
                super.setupRotations(body, poseStack, bob, yBodyRot, partialTick, scale);
                float target = body.isInWater() ? -90.0F - xRot : -90.0F;
                poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(swim, 0.0F, target)));
                if (body.isVisuallySwimming()) {
                    poseStack.translate(0.0F, -1.0F, 0.3F);
                }
            } else {
                super.setupRotations(body, poseStack, bob, yBodyRot, partialTick, scale);
            }
        }
    }

    static final class Cape extends RenderLayer<PossessionBodyEntity, PlayerModel<PossessionBodyEntity>> {
        Cape(RenderLayerParent<PossessionBodyEntity, PlayerModel<PossessionBodyEntity>> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffers, int light, PossessionBodyEntity body, float limbSwing, float limbSwingAmount,
            float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (body.isInvisible() || !body.isModelPartShown(PlayerModelPart.CAPE)) {
                return;
            }
            PlayerSkin skin = skin(body);
            if (skin.capeTexture() == null || body.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
                return;
            }
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.0F, 0.125F);
            double dx = body.getCloakX(partialTick) - Mth.lerp(partialTick, body.xo, body.getX());
            double dy = body.getCloakY(partialTick) - Mth.lerp(partialTick, body.yo, body.getY());
            double dz = body.getCloakZ(partialTick) - Mth.lerp(partialTick, body.zo, body.getZ());
            float bodyRot = Mth.rotLerp(partialTick, body.yBodyRotO, body.yBodyRot);
            double forwardX = Mth.sin(bodyRot * (float)(Math.PI / 180.0));
            double forwardZ = -Mth.cos(bodyRot * (float)(Math.PI / 180.0));
            float flap = Mth.clamp((float)dy * 10.0F, -6.0F, 32.0F);
            float lean = Mth.clamp((float)(dx * forwardX + dz * forwardZ) * 100.0F, 0.0F, 150.0F);
            float side = Mth.clamp((float)(dx * forwardZ - dz * forwardX) * 100.0F, -20.0F, 20.0F);
            if (body.isCrouching()) {
                flap += 25.0F;
            }
            poseStack.mulPose(Axis.XP.rotationDegrees(6.0F + lean / 2.0F + flap));
            poseStack.mulPose(Axis.ZP.rotationDegrees(side / 2.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - side / 2.0F));
            VertexConsumer consumer = buffers.getBuffer(RenderType.entitySolid(skin.capeTexture()));
            this.getParentModel().renderCloak(poseStack, consumer, light, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }

    static final class Wings extends RenderLayer<PossessionBodyEntity, PlayerModel<PossessionBodyEntity>> {
        private static final ResourceLocation WINGS_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/elytra.png");
        private final ElytraModel<PossessionBodyEntity> elytraModel;

        Wings(RenderLayerParent<PossessionBodyEntity, PlayerModel<PossessionBodyEntity>> parent, EntityModelSet modelSet) {
            super(parent);
            this.elytraModel = new ElytraModel<>(modelSet.bakeLayer(ModelLayers.ELYTRA));
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffers, int light, PossessionBodyEntity body, float limbSwing, float limbSwingAmount,
            float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            ItemStack chest = body.getItemBySlot(EquipmentSlot.CHEST);
            if (!chest.is(Items.ELYTRA)) {
                return;
            }
            PlayerSkin skin = skin(body);
            ResourceLocation texture;
            if (skin.elytraTexture() != null) {
                texture = skin.elytraTexture();
            } else if (skin.capeTexture() != null && body.isModelPartShown(PlayerModelPart.CAPE)) {
                texture = skin.capeTexture();
            } else {
                texture = WINGS_LOCATION;
            }
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.0F, 0.125F);
            this.getParentModel().copyPropertiesTo(this.elytraModel);
            this.elytraModel.setupAnim(body, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            VertexConsumer consumer = ItemRenderer.getArmorFoilBuffer(buffers, RenderType.armorCutoutNoCull(texture), chest.hasFoil());
            this.elytraModel.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }
}
