package net.yezon.theabyss.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.yezon.theabyss.entity.RiftNightbladeEntity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class RiftNightbladeModel extends GeoModel<RiftNightbladeEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath("theabyss", "geo/nightblade.geo.json");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath("theabyss", "animations/rift_nightblade.animation.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/entity/nightblade_boss.png");

    @Override
    public ResourceLocation getModelResource(RiftNightbladeEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(RiftNightbladeEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(RiftNightbladeEntity animatable) {
        return ANIMATIONS;
    }

    @Override
    public void setCustomAnimations(RiftNightbladeEntity animatable, long instanceId, AnimationState<RiftNightbladeEntity> animationState) {
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
