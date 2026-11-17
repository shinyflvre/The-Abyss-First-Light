package net.yezon.theabyss.world.teleporter;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.init.TheabyssModBlocks;
import org.jspecify.annotations.Nullable;

public class TheAbyssDimPortalShape {
    private static final int MIN_WIDTH = 2;
    public static final int MAX_WIDTH = 21;
    private static final int MIN_HEIGHT = 3;
    public static final int MAX_HEIGHT = 21;
    private static final Predicate<BlockState> FRAME = state -> state.is(TheabyssModBlocks.UNSTABLE_OBSIDIAN.get());
    private final BlockGetter level;
    private final Axis axis;
    private final Direction rightDir;
    private int numPortalBlocks;
    private @Nullable BlockPos bottomLeft;
    private int height;
    private final int width;

    public static Optional<TheAbyssDimPortalShape> findEmptyPortalShape(BlockGetter level, BlockPos pos, Axis preferredAxis) {
        return findPortalShape(level, pos, shape -> shape.isValid() && shape.numPortalBlocks == 0, preferredAxis);
    }

    public static Optional<TheAbyssDimPortalShape> findPortalShape(
        BlockGetter level, BlockPos pos, Predicate<TheAbyssDimPortalShape> isValid, Axis preferredAxis
    ) {
        Optional<TheAbyssDimPortalShape> firstAxis = Optional.of(new TheAbyssDimPortalShape(level, pos, preferredAxis)).filter(isValid);
        if (firstAxis.isPresent()) {
            return firstAxis;
        }

        Axis otherAxis = preferredAxis == Axis.X ? Axis.Z : Axis.X;
        return Optional.of(new TheAbyssDimPortalShape(level, pos, otherAxis)).filter(isValid);
    }

    public TheAbyssDimPortalShape(BlockGetter level, BlockPos pos, Axis axis) {
        this.level = level;
        this.axis = axis;
        this.rightDir = axis == Axis.X ? Direction.WEST : Direction.SOUTH;
        this.bottomLeft = this.calculateBottomLeft(pos);
        if (this.bottomLeft == null) {
            this.bottomLeft = pos;
            this.width = 1;
            this.height = 1;
        } else {
            this.width = this.calculateWidth();
            if (this.width > 0) {
                this.height = this.calculateHeight();
            }
        }
    }

    private @Nullable BlockPos calculateBottomLeft(BlockPos pos) {
        int minY = Math.max(this.level.getMinBuildHeight(), pos.getY() - MAX_HEIGHT);

        while (pos.getY() > minY && isEmpty(this.level.getBlockState(pos.below()))) {
            pos = pos.below();
        }

        Direction leftDir = this.rightDir.getOpposite();
        int edge = this.getDistanceUntilEdgeAboveFrame(pos, leftDir) - 1;
        return edge < 0 ? null : pos.relative(leftDir, edge);
    }

    private int calculateWidth() {
        int width = this.getDistanceUntilEdgeAboveFrame(this.bottomLeft, this.rightDir);
        return width >= MIN_WIDTH && width <= MAX_WIDTH ? width : 0;
    }

    private int getDistanceUntilEdgeAboveFrame(BlockPos pos, Direction direction) {
        MutableBlockPos blockPos = new MutableBlockPos();

        for (int i = 0; i <= MAX_WIDTH; i++) {
            blockPos.set(pos).move(direction, i);
            BlockState blockState = this.level.getBlockState(blockPos);
            if (!isEmpty(blockState)) {
                if (FRAME.test(blockState)) {
                    return i;
                }
                break;
            }

            if (!FRAME.test(this.level.getBlockState(blockPos.move(Direction.DOWN)))) {
                break;
            }
        }

        return 0;
    }

    private int calculateHeight() {
        MutableBlockPos pos = new MutableBlockPos();
        int height = this.getDistanceUntilTop(pos);
        return height >= MIN_HEIGHT && height <= MAX_HEIGHT && this.hasTopFrame(pos, height) ? height : 0;
    }

    private boolean hasTopFrame(MutableBlockPos pos, int height) {
        for (int i = 0; i < this.width; i++) {
            MutableBlockPos framePos = pos.set(this.bottomLeft).move(Direction.UP, height).move(this.rightDir, i);
            if (!FRAME.test(this.level.getBlockState(framePos))) {
                return false;
            }
        }

        return true;
    }

    private int getDistanceUntilTop(MutableBlockPos pos) {
        for (int height = 0; height < MAX_HEIGHT; height++) {
            pos.set(this.bottomLeft).move(Direction.UP, height).move(this.rightDir, -1);
            if (!FRAME.test(this.level.getBlockState(pos))) {
                return height;
            }

            pos.set(this.bottomLeft).move(Direction.UP, height).move(this.rightDir, this.width);
            if (!FRAME.test(this.level.getBlockState(pos))) {
                return height;
            }

            for (int i = 0; i < this.width; i++) {
                pos.set(this.bottomLeft).move(Direction.UP, height).move(this.rightDir, i);
                BlockState state = this.level.getBlockState(pos);
                if (!isEmpty(state)) {
                    return height;
                }

                if (state.is(TheabyssModBlocks.THE_ABYSS_PORTAL.get())) {
                    this.numPortalBlocks++;
                }
            }
        }

        return MAX_HEIGHT;
    }

    private static boolean isEmpty(BlockState state) {
        return state.isAir() || state.is(TheabyssModBlocks.THE_ABYSS_PORTAL.get());
    }

    public boolean isValid() {
        return this.bottomLeft != null && this.width >= MIN_WIDTH && this.width <= MAX_WIDTH && this.height >= MIN_HEIGHT && this.height <= MAX_HEIGHT;
    }

    public void createPortalBlocks(LevelAccessor level) {
        BlockState portalState = TheabyssModBlocks.THE_ABYSS_PORTAL.get().defaultBlockState().setValue(NetherPortalBlock.AXIS, this.axis);
        BlockPos.betweenClosed(this.bottomLeft, this.bottomLeft.relative(Direction.UP, this.height - 1).relative(this.rightDir, this.width - 1))
            .forEach(pos -> level.setBlock(pos, portalState, 18));
    }

    public boolean isComplete() {
        return this.isValid() && this.numPortalBlocks == this.width * this.height;
    }
}
