package net.yezon.theabyss.entity.model;

import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.yezon.theabyss.entity.LizardEntity;
import org.jspecify.annotations.Nullable;

public class LizardModel extends GeoModel<LizardEntity> {
    public static final DataTicket<Float> WALL_BLEND = DataTicket.create("theabyss_lizard_wall_blend", Float.class);
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("theabyss", "lizard");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("theabyss", "textures/entity/lizard.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(LizardEntity entity) {
        return MODEL;
    }

    @Override
    public void addAdditionalStateData(LizardEntity entity, @Nullable Object relatedObject, GeoRenderState renderState) {
        float partialTick = renderState.getOrDefaultGeckolibData(DataTickets.PARTIAL_TICK, 0.0F);
        renderState.addGeckolibData(WALL_BLEND, Mth.lerp(partialTick, entity.wallBlendO, entity.wallBlend));
    }
}
