package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.compat.ARGB;
import net.yezon.theabyss.entity.AbyssEchoEntity;

public class AbyssEchoRenderer extends HumanoidMobRenderer<AbyssEchoEntity, PlayerModel<AbyssEchoEntity>> {
    private static final int TINT = ARGB.color(190, 160, 225, 255);
    private final PlayerModel<AbyssEchoEntity> wide;
    private final PlayerModel<AbyssEchoEntity> slim;

    public AbyssEchoRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.0F);
        this.wide = this.model;
        this.slim = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    @Override
    public void render(AbyssEchoEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        EchoSkins skin = EchoSkins.of(entity.ownerId());
        this.model = skin.slim() ? this.slim : this.wide;
        super.render(entity, entityYaw, partialTicks, poseStack, TintedBuffers.of(buffers, RenderType.entityTranslucent(skin.texture()), TINT), light);
    }

    @Override
    public ResourceLocation getTextureLocation(AbyssEchoEntity entity) {
        return EchoSkins.of(entity.ownerId()).texture();
    }

    @Override
    protected RenderType getRenderType(AbyssEchoEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(this.getTextureLocation(entity));
    }
}
