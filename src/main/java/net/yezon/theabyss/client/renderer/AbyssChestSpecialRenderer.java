package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.yezon.theabyss.client.model.AbyssChestModel;
import org.joml.Vector3fc;

public class AbyssChestSpecialRenderer implements NoDataSpecialModelRenderer {
    private final AbyssChestModel model;
    private final String wood;

    public AbyssChestSpecialRenderer(AbyssChestModel model, String wood) {
        this.model = model;
        this.wood = wood;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        collector.submitModel(this.model, 0.0F, poseStack, this.model.renderType(AbyssChestRenderer.texture(this.wood, ChestType.SINGLE)),
            lightCoords, overlayCoords, outlineColor);
        collector.order(1).submitModel(this.model, 0.0F, poseStack, AbyssChestRenderer.glow(ChestType.SINGLE), AbyssChestRenderer.FULL_BRIGHT,
            overlayCoords, 0);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.model.setupAnim(0.0F);
        this.model.root().getExtentsForGui(poseStack, output);
    }

    public record Unbaked(String wood) implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(Codec.STRING.fieldOf("wood").forGetter(Unbaked::wood)).apply(i, Unbaked::new)
        );

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public AbyssChestSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new AbyssChestSpecialRenderer(new AbyssChestModel(context.entityModelSet().bakeLayer(AbyssChestModel.SINGLE)), this.wood);
        }
    }
}
