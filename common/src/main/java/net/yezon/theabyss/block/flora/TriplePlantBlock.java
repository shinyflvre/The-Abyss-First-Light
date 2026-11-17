package net.yezon.theabyss.block.flora;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jspecify.annotations.Nullable;

public class TriplePlantBlock extends BushBlock {
    public static final MapCodec<TriplePlantBlock> CODEC = simpleCodec(TriplePlantBlock::new);

    public enum Part implements StringRepresentable {
        LOWER("lower"),
        MIDDLE("middle"),
        UPPER("upper");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        int height() {
            return this.ordinal();
        }
    }

    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    public TriplePlantBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PART, Part.LOWER));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction directionToNeighbour, BlockState neighbourState, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        Part part = state.getValue(PART);
        if (directionToNeighbour == Direction.UP && part != Part.UPPER) {
            Part expected = part == Part.LOWER ? Part.MIDDLE : Part.UPPER;
            if (!neighbourState.is(this) || neighbourState.getValue(PART) != expected) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(state, directionToNeighbour, neighbourState, level, pos, neighbourPos);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        return pos.getY() < level.getMaxBuildHeight() - 2
            && level.getBlockState(pos.above()).canBeReplaced(context)
            && level.getBlockState(pos.above(2)).canBeReplaced(context)
            ? super.getStateForPlacement(context)
            : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
        level.setBlockAndUpdate(pos.above(), this.defaultBlockState().setValue(PART, Part.MIDDLE));
        level.setBlockAndUpdate(pos.above(2), this.defaultBlockState().setValue(PART, Part.UPPER));
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Part part = state.getValue(PART);
        if (part == Part.LOWER) {
            return super.canSurvive(state, level, pos);
        }
        BlockState below = level.getBlockState(pos.below());
        Part needed = part == Part.MIDDLE ? Part.LOWER : Part.MIDDLE;
        return below.is(this) && below.getValue(PART) == needed;
    }

    public static void placeAt(LevelAccessor level, BlockState state, BlockPos lowerPos, int updateType) {
        level.setBlock(lowerPos, state.setValue(PART, Part.LOWER), updateType);
        level.setBlock(lowerPos.above(), state.setValue(PART, Part.MIDDLE), updateType);
        level.setBlock(lowerPos.above(2), state.setValue(PART, Part.UPPER), updateType);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            if (player.getAbilities().instabuild) {
                preventDropFromLowerPart(level, pos, state, player);
            } else {
                dropResources(state, level, pos, null, player, player.getMainHandItem());
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack destroyedWith) {
        super.playerDestroy(level, player, pos, Blocks.AIR.defaultBlockState(), blockEntity, destroyedWith);
    }

    private static void preventDropFromLowerPart(Level level, BlockPos pos, BlockState state, Player player) {
        Part part = state.getValue(PART);
        if (part == Part.LOWER) {
            return;
        }
        BlockPos lowerPos = pos.below(part.height());
        BlockState lower = level.getBlockState(lowerPos);
        if (lower.is(state.getBlock()) && lower.getValue(PART) == Part.LOWER) {
            level.setBlock(lowerPos, Blocks.AIR.defaultBlockState(), 35);
            level.levelEvent(player, 2001, lowerPos, Block.getId(lower));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
    }

    @Override
    protected long getSeed(BlockState state, BlockPos pos) {
        return Mth.getSeed(pos.getX(), pos.below(state.getValue(PART).height()).getY(), pos.getZ());
    }

    @Override
    protected MapCodec<? extends TriplePlantBlock> codec() {
        return CODEC;
    }
}
