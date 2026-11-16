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
import net.yezon.theabyss.entity.AbyssaurEntity;
import net.yezon.theabyss.entity.model.AbyssaurModel;

public class AbyssaurRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<AbyssaurEntity, R> {
    private static final Identifier GLOW = Identifier.parse("theabyss:textures/entity/abyssaur_glow.png");
    private static final com.geckolib.constant.dataticket.DataTicket<Float> TURN = com.geckolib.constant.dataticket.DataTicket.create("theabyss_abyssaur_turn", Float.class);

    public AbyssaurRenderer(Context context) {
        super(context, new AbyssaurModel());
        this.shadowRadius = 1.2F;
        this.withRenderLayer(new TextureLayerGeoLayer<>(this, GLOW, RenderTypes::eyes));
    }

    @Override
    public void addRenderData(AbyssaurEntity animatable, @org.jspecify.annotations.Nullable Void relatedObject, R renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(TURN, animatable.turn(partialTick));
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        float pitch = renderPassInfo.getOrDefaultGeckolibData(DataTickets.ENTITY_PITCH, 0.0F);
        float yaw = renderPassInfo.getOrDefaultGeckolibData(DataTickets.ENTITY_YAW, 0.0F);
        snapshots.ifPresent("head", bone -> {
            bone.setRotX(bone.getRotX() - pitch * 0.6F * Mth.DEG_TO_RAD);
            bone.setRotY(bone.getRotY() - yaw * 0.6F * Mth.DEG_TO_RAD);
        });
        float turn = Mth.clamp(renderPassInfo.getOrDefaultGeckolibData(TURN, 0.0F), -8.0F, 8.0F);
        snapshots.ifPresent("tail", bone -> bone.setRotY(bone.getRotY() + turn * 2.0F * Mth.DEG_TO_RAD));
        snapshots.ifPresent("tail_mid", bone -> bone.setRotY(bone.getRotY() + turn * 2.5F * Mth.DEG_TO_RAD));
        snapshots.ifPresent("tail_end", bone -> bone.setRotY(bone.getRotY() + turn * 3.0F * Mth.DEG_TO_RAD));
        snapshots.ifPresent("torso", bone -> bone.setRotZ(bone.getRotZ() + turn * 1.2F * Mth.DEG_TO_RAD));
    }
}
