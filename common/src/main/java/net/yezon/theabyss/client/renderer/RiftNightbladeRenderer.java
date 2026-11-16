package net.yezon.theabyss.client.renderer;

import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.TextureLayerGeoLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.yezon.theabyss.entity.RiftNightbladeEntity;
import net.yezon.theabyss.entity.model.RiftNightbladeModel;

public class RiftNightbladeRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<RiftNightbladeEntity, R> {
    private static final Identifier GLOW = Identifier.parse("theabyss:textures/entity/nightblade_glowing_layer.png");

    public RiftNightbladeRenderer(Context context) {
        super(context, new RiftNightbladeModel());
        this.shadowRadius = 0.7F;
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
