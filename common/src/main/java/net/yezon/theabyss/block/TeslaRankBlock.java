package net.yezon.theabyss.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yezon.theabyss.platform.common.CommonHooks;

public class TeslaRankBlock extends SugarCaneBlock {
    public TeslaRankBlock(BlockBehaviour.Properties properties) {
        super(
            properties
                .mapColor(MapColor.PLANT)
                .randomTicks()
                .sound(SoundType.GRASS)
                .instabreak()
                .hasPostProcess((bs, br, bp) -> true)
                .emissiveRendering((bs, br, bp) -> true)
                .lightLevel(s -> 1)
                .noCollission()
                .offsetType(OffsetType.XZ)
                .pushReaction(PushReaction.DESTROY)
        );
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(this));
    }

    @Override
    protected void randomTick(BlockState blockstate, ServerLevel world, BlockPos blockpos, RandomSource random) {
        if (world.isEmptyBlock(blockpos.above())) {
            int i = 1;

            while (world.getBlockState(blockpos.below(i)).is(this)) {
                i++;
            }

            if (i < 14) {
                int j = blockstate.getValue(AGE);
                if (CommonHooks.canCropGrow(world, blockpos, blockstate, true)) {
                    if (j == 15) {
                        world.setBlockAndUpdate(blockpos.above(), this.defaultBlockState());
                        world.setBlock(blockpos, blockstate.setValue(AGE, 0), 4);
                    } else {
                        world.setBlock(blockpos, blockstate.setValue(AGE, j + 1), 4);
                    }
                }
            }
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return super.canSurvive(state, level, pos) || AbyssPlantSupport.supportsVegetation(level.getBlockState(pos.below()));
    }
}
