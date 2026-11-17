package net.yezon.theabyss.fabric.client;

import java.util.function.Supplier;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.platform.client.model.DynamicBlockStateModel;

final class FabricDynamicBlockStateModel extends ForwardingBakedModel {
    private final DynamicBlockStateModel model;

    FabricDynamicBlockStateModel(DynamicBlockStateModel model) {
        super(model.fallback().model());
        this.model = model;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter level, BlockState state, BlockPos pos, Supplier<RandomSource> random, RenderContext context) {
        DynamicBlockStateModel.Delegate delegate = this.model.select(level, pos, state);
        delegate.model().emitBlockQuads(level, delegate.state(), pos, random, context);
    }
}
