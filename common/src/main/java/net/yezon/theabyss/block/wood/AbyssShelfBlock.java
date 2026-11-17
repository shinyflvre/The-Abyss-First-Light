package net.yezon.theabyss.block.wood;

import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class AbyssShelfBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<AbyssShelfBlock> CODEC = simpleCodec(AbyssShelfBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<ShelfChainPart> SIDE_CHAIN_PART = EnumProperty.create("side_chain", ShelfChainPart.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final SoundEvent SOUND_ACTIVATE = SoundEvents.WOODEN_BUTTON_CLICK_ON;
    public static final SoundEvent SOUND_DEACTIVATE = SoundEvents.WOODEN_BUTTON_CLICK_OFF;
    public static final SoundEvent SOUND_PLACE_ITEM = SoundEvents.CHISELED_BOOKSHELF_INSERT;
    public static final SoundEvent SOUND_TAKE_ITEM = SoundEvents.CHISELED_BOOKSHELF_PICKUP;
    public static final SoundEvent SOUND_SINGLE_SWAP = SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED;
    public static final SoundEvent SOUND_MULTI_SWAP = SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED;
    private static final TagKey<Block> WOODEN_SHELVES = TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("wooden_shelves"));
    private static final int ROWS = 1;
    private static final int COLUMNS = 3;
    private static final int MAX_CHAIN_LENGTH = 3;
    private static final Map<Direction, VoxelShape> SHAPES = shapes();

    public AbyssShelfBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false)
                .setValue(SIDE_CHAIN_PART, ShelfChainPart.UNCONNECTED)
                .setValue(WATERLOGGED, false)
        );
    }

    private static Map<Direction, VoxelShape> shapes() {
        Map<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
        map.put(Direction.NORTH, Shapes.or(box(0.0, 12.0, 11.0, 16.0, 16.0, 13.0), box(0.0, 0.0, 13.0, 16.0, 16.0, 16.0), box(0.0, 0.0, 11.0, 16.0, 4.0, 13.0)));
        map.put(Direction.SOUTH, Shapes.or(box(0.0, 12.0, 3.0, 16.0, 16.0, 5.0), box(0.0, 0.0, 0.0, 16.0, 16.0, 3.0), box(0.0, 0.0, 3.0, 16.0, 4.0, 5.0)));
        map.put(Direction.EAST, Shapes.or(box(3.0, 12.0, 0.0, 5.0, 16.0, 16.0), box(0.0, 0.0, 0.0, 3.0, 16.0, 16.0), box(3.0, 0.0, 0.0, 5.0, 4.0, 16.0)));
        map.put(Direction.WEST, Shapes.or(box(11.0, 12.0, 0.0, 13.0, 16.0, 16.0), box(13.0, 0.0, 0.0, 16.0, 16.0, 16.0), box(11.0, 0.0, 0.0, 13.0, 4.0, 16.0)));
        return map;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return type == PathComputationType.WATER && state.getFluidState().is(FluidTags.WATER);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AbyssShelfBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, SIDE_CHAIN_PART, WATERLOGGED);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, movedByPiston);
            return;
        }
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
        this.updateNeighborsAfterPoweringDown(level, pos, state);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
        if (!level.isClientSide()) {
            boolean signal = level.hasNeighborSignal(pos);
            if (state.getValue(POWERED) != signal) {
                BlockState newState = state.setValue(POWERED, signal);
                if (!signal) {
                    newState = newState.setValue(SIDE_CHAIN_PART, ShelfChainPart.UNCONNECTED);
                }
                level.setBlockAndUpdate(pos, newState);
                this.playSound(level, pos, signal ? SOUND_ACTIVATE : SOUND_DEACTIVATE);
                level.gameEvent(signal ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos, GameEvent.Context.of(newState));
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState replacedFluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
            .setValue(FACING, context.getHorizontalDirection().getOpposite())
            .setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()))
            .setValue(WATERLOGGED, replacedFluidState.is(Fluids.WATER));
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof AbyssShelfBlockEntity shelf && !hand.equals(InteractionHand.OFF_HAND)) {
            OptionalInt hitSlot = getHitSlot(hitResult, state.getValue(FACING));
            if (hitSlot.isEmpty()) {
                return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
            }
            Inventory inventory = player.getInventory();
            if (level.isClientSide()) {
                return inventory.getSelected().isEmpty() ? ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION : ItemInteractionResult.CONSUME;
            }
            if (!state.getValue(POWERED)) {
                boolean itemRemoved = swapSingleItem(itemStack, player, shelf, hitSlot.getAsInt(), inventory);
                if (itemRemoved) {
                    this.playSound(level, pos, itemStack.isEmpty() ? SOUND_TAKE_ITEM : SOUND_SINGLE_SWAP);
                } else {
                    if (itemStack.isEmpty()) {
                        return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
                    }
                    this.playSound(level, pos, SOUND_PLACE_ITEM);
                }
                return ItemInteractionResult.SUCCESS;
            }
            if (!this.swapHotbar(level, pos, inventory)) {
                return ItemInteractionResult.CONSUME;
            }
            this.playSound(level, pos, SOUND_MULTI_SWAP);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }

    private static boolean swapSingleItem(ItemStack itemStack, Player player, AbyssShelfBlockEntity shelf, int hitSlot, Inventory inventory) {
        ItemStack removedItem = shelf.swapItemNoUpdate(hitSlot, itemStack);
        ItemStack newInventoryItem = player.hasInfiniteMaterials() && removedItem.isEmpty() ? itemStack.copy() : removedItem;
        inventory.setItem(inventory.selected, newInventoryItem);
        inventory.setChanged();
        shelf.setChanged(GameEvent.ITEM_INTERACT_FINISH);
        return !removedItem.isEmpty();
    }

    private boolean swapHotbar(Level level, BlockPos pos, Inventory inventory) {
        List<BlockPos> connectedBlocks = this.getAllBlocksConnectedTo(level, pos);
        if (connectedBlocks.isEmpty()) {
            return false;
        }
        boolean anySwapped = false;
        for (int shelfPartIndex = 0; shelfPartIndex < connectedBlocks.size(); shelfPartIndex++) {
            if (level.getBlockEntity(connectedBlocks.get(shelfPartIndex)) instanceof AbyssShelfBlockEntity shelfPart) {
                for (int slot = 0; slot < shelfPart.getContainerSize(); slot++) {
                    int inventorySlot = 9 - (connectedBlocks.size() - shelfPartIndex) * shelfPart.getContainerSize() + slot;
                    if (inventorySlot >= 0 && inventorySlot <= inventory.getContainerSize()) {
                        ItemStack placedInventoryItem = inventory.removeItemNoUpdate(inventorySlot);
                        ItemStack removedShelfItem = shelfPart.swapItemNoUpdate(slot, placedInventoryItem);
                        if (!placedInventoryItem.isEmpty() || !removedShelfItem.isEmpty()) {
                            inventory.setItem(inventorySlot, removedShelfItem);
                            anySwapped = true;
                        }
                    }
                }
                inventory.setChanged();
                shelfPart.setChanged(GameEvent.ENTITY_INTERACT);
            }
        }
        return anySwapped;
    }

    public boolean isConnectable(BlockState state) {
        return (state.getBlock() instanceof AbyssShelfBlock || state.is(WOODEN_SHELVES)) && state.hasProperty(POWERED) && state.getValue(POWERED);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (state.getValue(POWERED)) {
            this.updateSelfAndNeighborsOnPoweringUp(level, pos, state, oldState);
        } else {
            this.updateNeighborsAfterPoweringDown(level, pos, state);
        }
    }

    private void playSound(LevelAccessor level, BlockPos pos, SoundEvent sound) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (level.isClientSide() || direction != state.getValue(FACING).getOpposite()) {
            return 0;
        }
        return this.getAnalogOutputSignal(state, level, pos);
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return 0;
        }
        if (level.getBlockEntity(pos) instanceof AbyssShelfBlockEntity shelf) {
            int item1Bit = shelf.getItem(0).isEmpty() ? 0 : 1;
            int item2Bit = shelf.getItem(1).isEmpty() ? 0 : 1;
            int item3Bit = shelf.getItem(2).isEmpty() ? 0 : 1;
            return item1Bit | item2Bit << 1 | item3Bit << 2;
        }
        return 0;
    }

    private static OptionalInt getHitSlot(BlockHitResult hitResult, Direction blockFacing) {
        return relativeHitCoordinates(hitResult, blockFacing).map(hitCoords -> {
            int row = section(1.0F - hitCoords.y, ROWS);
            int column = section(hitCoords.x, COLUMNS);
            return OptionalInt.of(column + row * COLUMNS);
        }).orElseGet(OptionalInt::empty);
    }

    private static Optional<Vec2> relativeHitCoordinates(BlockHitResult hitResult, Direction blockFacing) {
        Direction hitDirection = hitResult.getDirection();
        if (blockFacing != hitDirection) {
            return Optional.empty();
        }
        BlockPos hitBlockPos = hitResult.getBlockPos().relative(hitDirection);
        Vec3 relativeHit = hitResult.getLocation().subtract(hitBlockPos.getX(), hitBlockPos.getY(), hitBlockPos.getZ());
        double relativeX = relativeHit.x();
        double relativeY = relativeHit.y();
        double relativeZ = relativeHit.z();
        return switch (hitDirection) {
            case NORTH -> Optional.of(new Vec2((float)(1.0 - relativeX), (float)relativeY));
            case SOUTH -> Optional.of(new Vec2((float)relativeX, (float)relativeY));
            case WEST -> Optional.of(new Vec2((float)relativeZ, (float)relativeY));
            case EAST -> Optional.of(new Vec2((float)(1.0 - relativeZ), (float)relativeY));
            case DOWN, UP -> Optional.empty();
        };
    }

    private static int section(float relativeCoordinate, int maxSections) {
        float targetedPixel = relativeCoordinate * 16.0F;
        float sectionSize = 16.0F / maxSections;
        return Mth.clamp(Mth.floor(targetedPixel / sectionSize), 0, maxSections - 1);
    }

    public List<BlockPos> getAllBlocksConnectedTo(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!this.isConnectable(state)) {
            return List.of();
        }
        Neighbors neighbors = new Neighbors(this, level, state.getValue(FACING), pos, new HashMap<>());
        LinkedList<BlockPos> results = new LinkedList<>();
        results.add(pos);
        this.addBlocksConnectingTowards(neighbors::left, ShelfChainPart.LEFT, results::addFirst);
        this.addBlocksConnectingTowards(neighbors::right, ShelfChainPart.RIGHT, results::addLast);
        return results;
    }

    private void addBlocksConnectingTowards(IntFunction<Neighbor> getNeighbor, ShelfChainPart endPart, Consumer<BlockPos> accumulator) {
        for (int steps = 1; steps < MAX_CHAIN_LENGTH; steps++) {
            Neighbor neighbor = getNeighbor.apply(steps);
            if (neighbor.connectsTowards(endPart)) {
                accumulator.accept(neighbor.pos());
            }
            if (neighbor.isUnconnectableOrChainEnd()) {
                break;
            }
        }
    }

    private void updateNeighborsAfterPoweringDown(LevelAccessor level, BlockPos pos, BlockState state) {
        Neighbors neighbors = new Neighbors(this, level, state.getValue(FACING), pos, new HashMap<>());
        neighbors.left(1).disconnectFromRight();
        neighbors.right(1).disconnectFromLeft();
    }

    private void updateSelfAndNeighborsOnPoweringUp(LevelAccessor level, BlockPos pos, BlockState state, BlockState oldState) {
        if (!this.isConnectable(state) || this.isBeingUpdatedByNeighbor(state, oldState)) {
            return;
        }
        Neighbors neighbors = new Neighbors(this, level, state.getValue(FACING), pos, new HashMap<>());
        ShelfChainPart newPartForSelf = ShelfChainPart.UNCONNECTED;
        int existingChainOnTheLeft = neighbors.left(1).isConnectable() ? this.getAllBlocksConnectedTo(level, neighbors.left(1).pos()).size() : 0;
        int existingChainOnTheRight = neighbors.right(1).isConnectable() ? this.getAllBlocksConnectedTo(level, neighbors.right(1).pos()).size() : 0;
        int currentChainLength = 1;
        if (canConnect(existingChainOnTheLeft, currentChainLength)) {
            newPartForSelf = newPartForSelf.whenConnectedToTheLeft();
            neighbors.left(1).connectToTheRight();
            currentChainLength += existingChainOnTheLeft;
        }
        if (canConnect(existingChainOnTheRight, currentChainLength)) {
            newPartForSelf = newPartForSelf.whenConnectedToTheRight();
            neighbors.right(1).connectToTheLeft();
        }
        setPart(level, pos, newPartForSelf);
    }

    private static boolean canConnect(int newBlocksToConnectTo, int currentChainLength) {
        return newBlocksToConnectTo > 0 && currentChainLength + newBlocksToConnectTo <= MAX_CHAIN_LENGTH;
    }

    private boolean isBeingUpdatedByNeighbor(BlockState state, BlockState oldState) {
        boolean isGettingConnected = state.getValue(SIDE_CHAIN_PART).isConnected();
        boolean hasBeenConnectedBefore = this.isConnectable(oldState) && oldState.getValue(SIDE_CHAIN_PART).isConnected();
        return isGettingConnected || hasBeenConnectedBefore;
    }

    private static void setPart(LevelAccessor level, BlockPos pos, ShelfChainPart newPart) {
        BlockState state = level.getBlockState(pos);
        if (state.getValue(SIDE_CHAIN_PART) != newPart) {
            level.setBlock(pos, state.setValue(SIDE_CHAIN_PART, newPart), Block.UPDATE_ALL);
        }
    }

    private interface Neighbor {
        BlockPos pos();

        boolean isConnectable();

        boolean isUnconnectableOrChainEnd();

        boolean connectsTowards(ShelfChainPart endPart);

        default void connectToTheRight() {
        }

        default void connectToTheLeft() {
        }

        default void disconnectFromRight() {
        }

        default void disconnectFromLeft() {
        }
    }

    private record EmptyNeighbor(BlockPos pos) implements Neighbor {
        @Override
        public boolean isConnectable() {
            return false;
        }

        @Override
        public boolean isUnconnectableOrChainEnd() {
            return true;
        }

        @Override
        public boolean connectsTowards(ShelfChainPart endPart) {
            return false;
        }
    }

    private record ChainNeighbor(LevelAccessor level, BlockPos pos, ShelfChainPart part) implements Neighbor {
        @Override
        public boolean isConnectable() {
            return true;
        }

        @Override
        public boolean isUnconnectableOrChainEnd() {
            return this.part.isChainEnd();
        }

        @Override
        public boolean connectsTowards(ShelfChainPart endPart) {
            return this.part.isConnectionTowards(endPart);
        }

        @Override
        public void connectToTheRight() {
            setPart(this.level, this.pos, this.part.whenConnectedToTheRight());
        }

        @Override
        public void connectToTheLeft() {
            setPart(this.level, this.pos, this.part.whenConnectedToTheLeft());
        }

        @Override
        public void disconnectFromRight() {
            setPart(this.level, this.pos, this.part.whenDisconnectedFromTheRight());
        }

        @Override
        public void disconnectFromLeft() {
            setPart(this.level, this.pos, this.part.whenDisconnectedFromTheLeft());
        }
    }

    private record Neighbors(AbyssShelfBlock block, LevelAccessor level, Direction facing, BlockPos center, Map<BlockPos, Neighbor> cache) {
        private boolean isConnectableToThisBlock(BlockState neighbor) {
            return this.block.isConnectable(neighbor) && neighbor.hasProperty(FACING) && neighbor.getValue(FACING) == this.facing;
        }

        private Neighbor createNewNeighbor(BlockPos pos) {
            BlockState neighbor = this.level.getBlockState(pos);
            if (this.isConnectableToThisBlock(neighbor) && neighbor.hasProperty(SIDE_CHAIN_PART)) {
                return new ChainNeighbor(this.level, pos, neighbor.getValue(SIDE_CHAIN_PART));
            }
            return new EmptyNeighbor(pos);
        }

        private Neighbor getOrCreateNeighbor(Direction direction, int steps) {
            return this.cache.computeIfAbsent(this.center.relative(direction, steps), this::createNewNeighbor);
        }

        public Neighbor left(int steps) {
            return this.getOrCreateNeighbor(this.facing.getClockWise(), steps);
        }

        public Neighbor right(int steps) {
            return this.getOrCreateNeighbor(this.facing.getCounterClockWise(), steps);
        }
    }
}
