package net.yezon.theabyss.block.flora;

import com.mojang.serialization.MapCodec;
import net.yezon.theabyss.block.AbyssShapes;
import net.minecraft.world.level.LevelAccessor;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HangingPlantBodyBlock extends GrowingPlantBodyBlock {
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    private static final VoxelShape SHAPE = AbyssShapes.column(14.0, 0.0, 16.0);
    private final Supplier<? extends Block> head;

    public HangingPlantBodyBlock(BlockBehaviour.Properties properties, Supplier<? extends Block> head) {
        this(properties, head, Direction.DOWN);
    }

    public HangingPlantBodyBlock(BlockBehaviour.Properties properties, Supplier<? extends Block> head, Direction growth) {
        super(properties, growth, SHAPE, false);
        this.head = head;
        this.registerDefaultState(this.stateDefinition.any().setValue(TOP, false));
    }

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return (GrowingPlantHeadBlock)this.head.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TOP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction directionToNeighbour, BlockState neighbourState, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        BlockState updated = super.updateShape(state, directionToNeighbour, neighbourState, level, pos, neighbourPos);
        return updated.is(this) ? withTop(updated, level, pos) : updated;
    }

    static BlockState withTop(BlockState state, BlockGetter level, BlockPos pos) {
        HangingPlantBodyBlock body = (HangingPlantBodyBlock)state.getBlock();
        BlockState anchor = level.getBlockState(pos.relative(body.growthDirection.getOpposite()));
        return state.setValue(TOP, !anchor.is(body) && !anchor.is(body.head.get()));
    }

    @Override
    protected MapCodec<? extends GrowingPlantBodyBlock> codec() {
        return MapCodec.unit(this);
    }
}
