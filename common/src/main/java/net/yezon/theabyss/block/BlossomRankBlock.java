package net.yezon.theabyss.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yezon.theabyss.platform.common.CommonHooks;
import net.yezon.theabyss.init.TheabyssModBlocks;
import org.jspecify.annotations.Nullable;

public class BlossomRankBlock extends SugarCaneBlock {
    public static final EnumProperty<Segment> SEGMENT = EnumProperty.create("segment", Segment.class);
    public static final int MAX_HEIGHT = 12;
    public static final int PLANTED_HEIGHT = 3;

    public BlossomRankBlock(BlockBehaviour.Properties properties) {
        this(properties, true);
    }

    protected BlossomRankBlock(BlockBehaviour.Properties properties, boolean givesLight) {
        super(
            properties
                .mapColor(MapColor.PLANT)
                .randomTicks()
                .sound(SoundType.GRASS)
                .instabreak()
                .noCollision()
                .noOcclusion()
                .lightLevel(s -> !givesLight ? 0 : s.getValue(SEGMENT) == Segment.END ? 10 : 1)
                .offsetType(OffsetType.XZ)
                .pushReaction(PushReaction.POPPED)
        );
        this.registerDefaultState(this.defaultBlockState().setValue(SEGMENT, Segment.END));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SEGMENT);
    }

    private BlockState withSegment(BlockState state, BlockGetter level, BlockPos pos) {
        Segment segment;
        if (!level.getBlockState(pos.above()).is(this)) {
            segment = Segment.END;
        } else if (!level.getBlockState(pos.above(2)).is(this)) {
            segment = Segment.CLOSE_END;
        } else {
            segment = level.getBlockState(pos.below()).is(this) ? Segment.MIDDLE : Segment.START;
        }
        return state.setValue(SEGMENT, segment);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos.below()).is(this)) {
            if (pos.getY() + PLANTED_HEIGHT - 1 > level.getMaxY()) {
                return null;
            }
            for (int i = 1; i < PLANTED_HEIGHT; i++) {
                if (!level.getBlockState(pos.above(i)).canBeReplaced(context)) {
                    return null;
                }
            }
        }
        return withSegment(this.defaultBlockState(), level, pos);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
        if (level.getBlockState(pos.below()).is(this)) {
            return;
        }
        for (int i = 1; i < PLANTED_HEIGHT; i++) {
            level.setBlock(pos.above(i), this.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour,
        BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        BlockState updated = super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
        return updated.is(this) ? withSegment(updated, level, pos) : updated;
    }

    public static boolean isSoil(BlockState ground) {
        return ground.is(TheabyssModBlocks.NIGHTGROVE_MOSS.get()) || ground.is(TheabyssModBlocks.NIGHTGROVE_DIRT.get());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState belowState = level.getBlockState(pos.below());
        return belowState.is(this) || isSoil(belowState);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isEmptyBlock(pos.above())) {
            return;
        }
        int height = 1;
        while (level.getBlockState(pos.below(height)).is(this)) {
            height++;
        }
        if (height >= MAX_HEIGHT || !CommonHooks.canCropGrow(level, pos, state, true)) {
            return;
        }
        int age = state.getValue(AGE);
        if (age == 15) {
            BlockPos above = pos.above();
            level.setBlockAndUpdate(above, withSegment(this.defaultBlockState(), level, above));
            CommonHooks.fireCropGrowPost(level, above, level.getBlockState(above));
            level.setBlock(pos, level.getBlockState(pos).setValue(AGE, 0), 4);
        } else {
            level.setBlock(pos, state.setValue(AGE, age + 1), 4);
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return Collections.singletonList(new ItemStack(this));
    }

    public enum Segment implements StringRepresentable {
        START("start"),
        MIDDLE("middle"),
        CLOSE_END("close_end"),
        END("end");

        private final String name;

        Segment(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
