package net.yezon.theabyss.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.block.hooks.FireSpreadingBlock;

public class RenaShroomLeaveBlock extends Block implements FireSpreadingBlock {
    public RenaShroomLeaveBlock(BlockBehaviour.Properties properties) {
        super(
            properties
                .ignitedByLava()
                .sound(SoundType.CHERRY_LEAVES)
                .strength(0.3F, 5.0F)
                .postProcess((bs, br, bp) -> bp)
        );
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return 0;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 1;
    }
}
