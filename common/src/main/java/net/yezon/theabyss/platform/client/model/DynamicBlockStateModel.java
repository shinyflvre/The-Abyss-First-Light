package net.yezon.theabyss.platform.client.model;

import java.util.List;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.platform.services.ClientPlatformHooks;

public interface DynamicBlockStateModel extends BlockStateModel {
    @Override
    @Deprecated
    default void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), random, parts);
    }

    void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts);

    Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state);

    int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state);

    static BlockStateModel wrap(DynamicBlockStateModel model) {
        return ClientPlatformHooks.get().dynamicModel(model);
    }

    static void collectPartsOf(BlockStateModel model, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
        List<BlockStateModelPart> parts) {
        ClientPlatformHooks.get().collectParts(model, level, pos, state, random, parts);
    }

    static Material.Baked particleMaterialOf(BlockStateModel model, BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return ClientPlatformHooks.get().particleMaterial(model, level, pos, state);
    }
}
