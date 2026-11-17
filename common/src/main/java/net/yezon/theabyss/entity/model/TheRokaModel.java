package net.yezon.theabyss.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.yezon.theabyss.entity.TheRokaEntity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class TheRokaModel extends GeoModel<TheRokaEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath("theabyss", "geo/the_roka.geo.json");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath("theabyss", "animations/the_roka.animation.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/entity/the_roka.png");

    @Override
    public ResourceLocation getModelResource(TheRokaEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(TheRokaEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(TheRokaEntity animatable) {
        return ANIMATIONS;
    }

    @Override
    public void setCustomAnimations(TheRokaEntity animatable, long instanceId, AnimationState<TheRokaEntity> animationState) {
        EntityModelData data = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        float pitch = -data.headPitch();
        float yaw = Mth.wrapDegrees(-data.netHeadYaw());
        GeoBone head = this.getAnimationProcessor().getBone("head");
        if (head != null) {
            head.setRotX(head.getRotX() - pitch * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() - yaw * Mth.DEG_TO_RAD);
            head.resetStateChanges();
        }
    }
}
