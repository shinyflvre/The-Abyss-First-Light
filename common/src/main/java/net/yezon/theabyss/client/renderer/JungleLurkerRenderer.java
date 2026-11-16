package net.yezon.theabyss.client.renderer;

import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.TextureLayerGeoLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.yezon.theabyss.entity.JungleLurkerEntity;
import net.yezon.theabyss.entity.model.LurkerModel;
import org.jspecify.annotations.Nullable;

public class JungleLurkerRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<JungleLurkerEntity, R> {
    private static final Identifier GLOW = Identifier.parse("theabyss:textures/entity/lurker_glow.png");
    private static final DataTicket<Float> CAMO = DataTicket.create("theabyss_lurker_camo", Float.class);
    private static final DataTicket<Float> AGE = DataTicket.create("theabyss_lurker_age", Float.class);
    private static final float SHADOW = 0.5F;

    public JungleLurkerRenderer(Context context) {
        super(context, new LurkerModel());
        this.shadowRadius = SHADOW;
        this.withRenderLayer(new TextureLayerGeoLayer<>(this, GLOW, RenderTypes::eyes) {
            @Override
            protected @Nullable RenderType getRenderType(R state) {
                float camo = state.getOrDefaultGeckolibData(CAMO, 0.0F);
                if (camo > 0.55F) {
                    float age = state.getOrDefaultGeckolibData(AGE, 0.0F);
                    return Mth.sin(age * 0.37F) * Mth.sin(age * 0.11F) > 0.82F ? super.getRenderType(state) : null;
                }
                return super.getRenderType(state);
            }
        });
    }

    @Override
    public void addRenderData(JungleLurkerEntity animatable, @Nullable Void relatedObject, R renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(CAMO, animatable.camouflage(partialTick));
        renderState.addGeckolibData(AGE, animatable.tickCount + partialTick);
    }

    @Override
    public void extractRenderState(JungleLurkerEntity entity, R state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.shadowRadius = SHADOW * (1.0F - entity.camouflage(partialTick));
    }

    @Override
    public @Nullable RenderType getRenderType(R renderState, Identifier texture) {
        return renderState.getOrDefaultGeckolibData(CAMO, 0.0F) > 0.01F ? RenderTypes.entityTranslucent(texture) : super.getRenderType(renderState, texture);
    }

    @Override
    public int getRenderColor(JungleLurkerEntity animatable, @Nullable Void relatedObject, float partialTick) {
        int color = super.getRenderColor(animatable, relatedObject, partialTick);
        float camo = animatable.camouflage(partialTick);
        if (camo <= 0.01F) {
            return color;
        }
        float age = animatable.tickCount + partialTick;
        float shimmer = 0.75F + 0.25F * Mth.sin(age * 0.45F) * Mth.sin(age * 0.17F + 1.3F);
        float alpha = (1.0F - 0.9F * camo) * (camo > 0.5F ? shimmer : 1.0F);
        int r = Mth.lerpInt(camo, 255, 175);
        int g = Mth.lerpInt(camo, 255, 238);
        int b = Mth.lerpInt(camo, 255, 245);
        return ARGB.color(Mth.clamp(Math.round(ARGB.alpha(color) * alpha), 8, 255), r, g, b);
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        float pitch = renderPassInfo.getOrDefaultGeckolibData(DataTickets.ENTITY_PITCH, 0.0F);
        float yaw = renderPassInfo.getOrDefaultGeckolibData(DataTickets.ENTITY_YAW, 0.0F);
        snapshots.ifPresent("head", bone -> {
            bone.setRotX(bone.getRotX() - pitch * 0.6F * Mth.DEG_TO_RAD);
            bone.setRotY(bone.getRotY() - yaw * 0.6F * Mth.DEG_TO_RAD);
        });
    }
}
