package net.yezon.theabyss.block;

import net.minecraft.core.BlockPos;
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
import net.yezon.theabyss.init.TheabyssModBlocks;

public class TallNightgroveGrassBlock extends DoublePlantBlock {
    public TallNightgroveGrassBlock(BlockBehaviour.Properties properties) {
        super(
            properties
                .mapColor(MapColor.PLANT)
                .sound(SoundType.MOSS_CARPET)
                .instabreak()
                .noCollission()
                .replaceable()
                .offsetType(OffsetType.XYZ)
                .pushReaction(PushReaction.DESTROY)
        );
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
