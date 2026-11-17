package net.yezon.theabyss.platform.client.model;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.platform.services.ClientPlatformHooks;

public interface DynamicBlockStateModel {
    Delegate select(BlockAndTintGetter level, BlockPos pos, BlockState state);

    Delegate fallback();

    record Delegate(BakedModel model, BlockState state) {
    }

    static BakedModel wrap(DynamicBlockStateModel model) {
        return ClientPlatformHooks.get().dynamicModel(model);
    }
}
