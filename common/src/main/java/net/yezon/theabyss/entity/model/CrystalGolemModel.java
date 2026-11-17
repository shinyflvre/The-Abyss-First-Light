package net.yezon.theabyss.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.yezon.theabyss.entity.CrystalGolemEntity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class CrystalGolemModel extends GeoModel<CrystalGolemEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath("theabyss", "geo/crystal_golem.geo.json");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath("theabyss", "animations/crystal_golem.animation.json");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/entity/crystal_golem.png");

    @Override
    public ResourceLocation getModelResource(CrystalGolemEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(CrystalGolemEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(CrystalGolemEntity animatable) {
        return ANIMATIONS;
    }

    @Override
    public void setCustomAnimations(CrystalGolemEntity animatable, long instanceId, AnimationState<CrystalGolemEntity> animationState) {
        EntityModelData data = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        float pitch = -data.headPitch();
        float yaw = Mth.wrapDegrees(-data.netHeadYaw());
        GeoBone head = this.getAnimationProcessor().getBone("mob_head");
        if (head != null) {
            head.setRotX(head.getRotX() - pitch * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() - yaw * Mth.DEG_TO_RAD);
            head.resetStateChanges();
        }
    }
}
