package net.yezon.theabyss.block;

import net.minecraft.world.level.LevelAccessor;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yezon.theabyss.block.entity.GravityStoneBlockEntity;
import net.yezon.theabyss.gravity.Gravity;
import net.yezon.theabyss.gravity.GravityMoteOptions;
import org.jspecify.annotations.Nullable;

public class ChiseledGravityStoneBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    private static final BooleanProperty[] RIMS = {
        BlockStateProperties.DOWN, BlockStateProperties.UP, BlockStateProperties.NORTH,
        BlockStateProperties.SOUTH, BlockStateProperties.WEST, BlockStateProperties.EAST
    };

    public ChiseledGravityStoneBlock(BlockBehaviour.Properties properties) {
        super(properties.instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.STONE).strength(3.0F, 10.0F).requiresCorrectToolForDrops());
        BlockState state = this.stateDefinition.any().setValue(FACING, Direction.UP);
        for (BooleanProperty rim : RIMS) {
            state = state.setValue(rim, false);
        }
        this.registerDefaultState(state);
    }

    public static BooleanProperty rim(Direction side) {
        return RIMS[side.get3DDataValue()];
    }

    public static boolean covered(BlockGetter level, BlockPos pos, Direction facing) {
        BlockPos front = pos.relative(facing);
        if (level instanceof LevelReader reader && !reader.hasChunkAt(front)) {
            return false;
        }
        return level.getBlockState(front).isFaceSturdy(level, front, facing.getOpposite());
    }

    public static boolean flush(BlockGetter level, BlockPos pos, Direction facing, Direction side) {
        BlockPos beside = pos.relative(facing).relative(side);
        if (level instanceof LevelReader reader && !reader.hasChunkAt(beside)) {
            return false;
        }
        return level.getBlockState(beside).isFaceSturdy(level, beside, side.getOpposite());
    }

    public static BlockState shownRims(BlockGetter level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        BlockState shown = state;
        for (Direction side : Direction.values()) {
            BooleanProperty rim = rim(side);
            if (state.getValue(rim) && flush(level, pos, facing, side)) {
                shown = shown.setValue(rim, false);
            }
        }
        return shown;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(RIMS);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState().setValue(FACING, context.getClickedFace());
        return rims(state, context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction directionToNeighbour, BlockState neighbourState, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return rims(state, level, pos);
    }

    private static BlockState rims(BlockState state, BlockGetter level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        boolean open = !covered(level, pos, facing);
        for (Direction side : Direction.values()) {
            boolean edge = false;
            if (open && side.getAxis() != facing.getAxis()) {
                BlockState next = level.getBlockState(pos.relative(side));
                edge = !(next.getBlock() instanceof ChiseledGravityStoneBlock) || next.getValue(FACING) != facing;
            }
            state = state.setValue(rim(side), edge);
        }
        return state;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        BlockState turned = state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
        for (Direction side : Direction.values()) {
            turned = turned.setValue(rim(rotation.rotate(side)), state.getValue(rim(side)));
        }
        return turned;
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        BlockState flipped = state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
        for (Direction side : Direction.values()) {
            flipped = flipped.setValue(rim(mirror.mirror(side)), state.getValue(rim(side)));
        }
        return flipped;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) >= 3) {
            return;
        }
        Direction facing = state.getValue(FACING);
        if (covered(level, pos, facing)) {
            return;
        }
        Direction.Axis axis = facing.getAxis();
        double faceX = pos.getX() + 0.5 + facing.getStepX() * 0.5;
        double faceY = pos.getY() + 0.5 + facing.getStepY() * 0.5;
        double faceZ = pos.getZ() + 0.5 + facing.getStepZ() * 0.5;
        double u = random.nextDouble() - 0.5;
        double v = random.nextDouble() - 0.5;
        double tx = axis == Direction.Axis.X ? 0.0 : u;
        double ty = axis == Direction.Axis.Y ? 0.0 : (axis == Direction.Axis.X ? u : v);
        double tz = axis == Direction.Axis.Z ? 0.0 : v;
        double out = 0.7 + random.nextDouble() * (Gravity.RANGE - 0.9);
        double endX = faceX + tx + facing.getStepX() * 0.03;
        double endY = faceY + ty + facing.getStepY() * 0.03;
        double endZ = faceZ + tz + facing.getStepZ() * 0.03;
        double startX = endX + facing.getStepX() * out;
        double startY = endY + facing.getStepY() * out;
        double startZ = endZ + facing.getStepZ() * out;
        level.addParticle(new GravityMoteOptions(facing.getOpposite().get3DDataValue()), startX, startY, startZ,
            endX - startX, endY - startY, endZ - startZ);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        return !drops.isEmpty() ? drops : Collections.singletonList(new ItemStack(this, 1));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GravityStoneBlockEntity(pos, state);
    }
}
