package net.yezon.theabyss.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.yezon.theabyss.entity.JungleLurkerEntity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class LurkerModel extends GeoModel<JungleLurkerEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath("theabyss", "geo/lurker.geo.json");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath("theabyss", "animations/lurker.animation.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/entity/lurker.png");

    @Override
    public ResourceLocation getModelResource(JungleLurkerEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(JungleLurkerEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(JungleLurkerEntity animatable) {
        return ANIMATIONS;
    }

    @Override
    public void setCustomAnimations(JungleLurkerEntity animatable, long instanceId, AnimationState<JungleLurkerEntity> animationState) {
        EntityModelData data = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        float pitch = -data.headPitch();
        float yaw = Mth.wrapDegrees(-data.netHeadYaw());
        GeoBone head = this.getAnimationProcessor().getBone("head");
        if (head != null) {
            head.setRotX(head.getRotX() - pitch * 0.6F * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() - yaw * 0.6F * Mth.DEG_TO_RAD);
            head.resetStateChanges();
        }
    }
}
