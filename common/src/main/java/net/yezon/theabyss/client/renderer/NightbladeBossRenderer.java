package net.yezon.theabyss.client.renderer;

import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import com.geckolib.renderer.layer.builtin.TextureLayerGeoLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.yezon.theabyss.entity.NightbladeBossEntity;
import net.yezon.theabyss.entity.model.NightbladeModel;

public class NightbladeBossRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<NightbladeBossEntity, R> {
    private static final Identifier GLOW = Identifier.parse("theabyss:textures/entity/nightblade_glowing_layer.png");

    public NightbladeBossRenderer(Context context) {
        super(context, new NightbladeModel());
        this.shadowRadius = 0.7F;
        this.withRenderLayer(new ItemInHandGeoLayer<>(context, this) {
            @Override
            protected void submitItemStackRender(com.mojang.blaze3d.vertex.PoseStack poseStack, com.geckolib.cache.model.GeoBone bone,
                                                 net.minecraft.client.renderer.item.ItemStackRenderState stackState, net.minecraft.world.item.ItemDisplayContext displayContext,
                                                 R renderState, net.minecraft.client.renderer.SubmitNodeCollector renderTasks, int packedLight) {
                super.submitItemStackRender(poseStack, bone, stackState, displayContext, renderState, renderTasks, 0xF000F0);
            }
        });
        this.withRenderLayer(new TextureLayerGeoLayer<>(this, GLOW, RenderTypes::eyes));
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        float pitch = renderPassInfo.getOrDefaultGeckolibData(DataTickets.ENTITY_PITCH, 0.0F);
        float yaw = renderPassInfo.getOrDefaultGeckolibData(DataTickets.ENTITY_YAW, 0.0F);
        snapshots.ifPresent("head", bone -> {
            bone.setRotX(bone.getRotX() - pitch * Mth.DEG_TO_RAD);
            bone.setRotY(bone.getRotY() - yaw * Mth.DEG_TO_RAD);
        });
    }
}
