package net.yezon.theabyss.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirtPathBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jspecify.annotations.Nullable;

public class AbyssPathBlock extends DirtPathBlock {
    private final Block baseBlock;

    protected AbyssPathBlock(Block baseBlock, BlockBehaviour.Properties properties) {
        super(properties);
        this.baseBlock = baseBlock;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        this.turnToBaseBlock(null, state, level, pos);
    }

    public void turnToBaseBlock(@Nullable Entity sourceEntity, BlockState state, Level level, BlockPos pos) {
        BlockState newState = pushEntitiesUp(state, this.baseBlock.defaultBlockState(), level, pos);
        level.setBlockAndUpdate(pos, newState);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(sourceEntity, newState));
    }
}
