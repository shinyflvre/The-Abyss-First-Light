package net.yezon.theabyss.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.yezon.theabyss.entity.AbyssaurEntity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class AbyssaurModel extends GeoModel<AbyssaurEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath("theabyss", "geo/abyssaur.geo.json");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath("theabyss", "animations/abyssaur.animation.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/entity/abyssaur.png");

    @Override
    public ResourceLocation getModelResource(AbyssaurEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(AbyssaurEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(AbyssaurEntity animatable) {
        return ANIMATIONS;
    }

    @Override
    public void setCustomAnimations(AbyssaurEntity animatable, long instanceId, AnimationState<AbyssaurEntity> animationState) {
        EntityModelData data = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        float pitch = -data.headPitch();
        float yaw = Mth.wrapDegrees(-data.netHeadYaw());
        GeoBone head = this.getAnimationProcessor().getBone("head");
        if (head != null) {
            head.setRotX(head.getRotX() - pitch * 0.6F * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() - yaw * 0.6F * Mth.DEG_TO_RAD);
            head.resetStateChanges();
        }
        float turn = Mth.clamp(animatable.turn(animationState.getPartialTick()), -8.0F, 8.0F);
        this.addRot("tail", 0.0F, turn * 2.0F * Mth.DEG_TO_RAD, 0.0F);
        this.addRot("tail_mid", 0.0F, turn * 2.5F * Mth.DEG_TO_RAD, 0.0F);
        this.addRot("tail_end", 0.0F, turn * 3.0F * Mth.DEG_TO_RAD, 0.0F);
        this.addRot("torso", 0.0F, 0.0F, turn * 1.2F * Mth.DEG_TO_RAD);
    }

    private void addRot(String name, float x, float y, float z) {
        GeoBone bone = this.getAnimationProcessor().getBone(name);
        if (bone != null) {
            bone.setRotX(bone.getRotX() + x);
            bone.setRotY(bone.getRotY() + y);
            bone.setRotZ(bone.getRotZ() + z);
            bone.resetStateChanges();
        }
    }
}
