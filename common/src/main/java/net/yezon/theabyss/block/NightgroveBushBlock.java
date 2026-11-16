package net.yezon.theabyss.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.yezon.theabyss.init.TheabyssModBlocks;

public class NightgroveBushBlock extends BushBlock {
    public NightgroveBushBlock(BlockBehaviour.Properties properties) {
        super(properties.mapColor(MapColor.PLANT).replaceable().noCollision().instabreak().sound(SoundType.GRASS).ignitedByLava()
            .pushReaction(PushReaction.POPPED));
    }

    @Override
    protected boolean mayPlaceOn(BlockState ground, BlockGetter level, BlockPos pos) {
        return ground.is(TheabyssModBlocks.NIGHTGROVE_MOSS.get()) || ground.is(TheabyssModBlocks.NIGHTGROVE_DIRT.get()) || super.mayPlaceOn(ground, level, pos);
    }
}
