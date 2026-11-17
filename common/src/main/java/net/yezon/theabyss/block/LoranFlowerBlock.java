package net.yezon.theabyss.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yezon.theabyss.init.TheabyssModBlocks;
import net.yezon.theabyss.init.TheabyssModItems;

public class LoranFlowerBlock extends DoublePlantBlock {
    public LoranFlowerBlock(BlockBehaviour.Properties properties) {
        super(
            properties
                .mapColor(MapColor.PLANT)
                .sound(SoundType.GRASS)
                .instabreak()
                .hasPostProcess((bs, br, bp) -> true)
                .emissiveRendering((bs, br, bp) -> true)
                .lightLevel(s -> 10)
                .noCollission()
                .offsetType(OffsetType.XZ)
                .pushReaction(PushReaction.DESTROY)
        );
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (state.getValue(HALF) != DoubleBlockHalf.LOWER) {
            return Collections.emptyList();
        }

        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(TheabyssModItems.LORAN.get(), builder.getLevel().getRandom().nextIntBetweenInclusive(1, 2)));
    }

    @Override
    protected boolean mayPlaceOn(BlockState groundState, BlockGetter worldIn, BlockPos pos) {
        return groundState.is(TheabyssModBlocks.NIGHTGROVE_MOSS.get()) || groundState.is(TheabyssModBlocks.NIGHTGROVE_DIRT.get());
    }

    @Override
    protected boolean canSurvive(BlockState blockstate, LevelReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.below();
        BlockState groundState = worldIn.getBlockState(blockpos);
        return blockstate.getValue(HALF) != DoubleBlockHalf.UPPER
            ? this.mayPlaceOn(groundState, worldIn, blockpos)
            : groundState.is(this) && groundState.getValue(HALF) == DoubleBlockHalf.LOWER;
    }
}
