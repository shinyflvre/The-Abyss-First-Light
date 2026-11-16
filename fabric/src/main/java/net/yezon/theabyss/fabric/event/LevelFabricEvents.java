package net.yezon.theabyss.fabric.event;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.event.level.BlockDropsEvent;
import net.yezon.theabyss.platform.event.level.BlockEvent;
import net.yezon.theabyss.platform.event.level.ChunkEvent;
import net.yezon.theabyss.platform.event.level.ExplosionEvent;
import net.yezon.theabyss.platform.event.level.block.BreakBlockEvent;
import org.jspecify.annotations.Nullable;

public final class LevelFabricEvents {
    private static @Nullable List<ItemEntity> capturedBlockDrops;

    private LevelFabricEvents() {
    }

    public static void chunkUnload(LevelChunk chunk) {
        if (AbyssEventBus.hasListeners(ChunkEvent.Unload.class)) {
            AbyssEventBus.post(new ChunkEvent.Unload(chunk, chunk.getLevel()));
        }
    }

    public static boolean breakBlock(Level level, GameType gameType, Player player, BlockPos pos, BlockState state) {
        if (!AbyssEventBus.hasListeners(BreakBlockEvent.class)) {
            return false;
        }
        boolean preCancel = false;
        ItemStack held = player.getMainHandItem();
        if (!held.isEmpty() && !held.canDestroyBlock(state, level, pos, player)) {
            preCancel = true;
        }
        if (player.blockActionRestricted(level, pos, gameType)) {
            preCancel = true;
        }
        if (state.getBlock() instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
            preCancel = true;
        }
        BreakBlockEvent event = new BreakBlockEvent(level, player, pos);
        event.setCanceled(preCancel);
        AbyssEventBus.post(event);
        if (event.isCanceled() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundBlockUpdatePacket(pos, state));
        }
        return event.isCanceled();
    }

    public static void explosionDetonate(Level level, @Nullable List<BlockPos> blocks) {
        if (blocks != null && AbyssEventBus.hasListeners(ExplosionEvent.Detonate.class)) {
            AbyssEventBus.post(new ExplosionEvent.Detonate(blocks, level));
        }
    }

    public static boolean beginBlockDrops() {
        if (!AbyssEventBus.hasListeners(BlockDropsEvent.class)) {
            return false;
        }
        capturedBlockDrops = new ArrayList<>();
        return true;
    }

    public static boolean captureBlockDrop(ItemEntity drop) {
        List<ItemEntity> drops = capturedBlockDrops;
        if (drops == null) {
            return false;
        }
        drops.add(drop);
        return true;
    }

    public static boolean finishBlockDrops(ServerLevel level, BlockPos pos, BlockState state, ItemStack tool) {
        List<ItemEntity> drops = capturedBlockDrops;
        capturedBlockDrops = null;
        if (drops == null) {
            return true;
        }
        BlockDropsEvent event = AbyssEventBus.post(new BlockDropsEvent(drops, level, pos, state, tool));
        if (event.isCanceled()) {
            return false;
        }
        for (ItemEntity drop : event.getDrops()) {
            level.addFreshEntity(drop);
        }
        return true;
    }

    public static boolean isRestoring(Level level) {
        return ((FabricBlockCaptureState)level).theabyss$restoring();
    }

    public static boolean isCapturing(Level level) {
        return ((FabricBlockCaptureState)level).theabyss$capturing();
    }

    public static boolean enderPlace(Entity entity, Level level, BlockPos pos) {
        return AbyssEventBus.hasListeners(BlockEvent.EntityPlaceEvent.class)
            && AbyssEventBus.post(new BlockEvent.EntityPlaceEvent(entity, level, pos)).isCanceled();
    }

    public static @Nullable FabricBlockSnapshot snapshotBeforeSet(Level level, BlockPos pos, int flags) {
        if (level.isClientSide() || !isCapturing(level)) {
            return null;
        }
        FabricBlockSnapshot snapshot = FabricBlockSnapshot.create(level, pos, flags);
        ((FabricBlockCaptureState)level).theabyss$snapshots().add(snapshot);
        return snapshot;
    }

    public static void dropSnapshot(Level level, FabricBlockSnapshot snapshot) {
        ((FabricBlockCaptureState)level).theabyss$snapshots().remove(snapshot);
    }

    public static InteractionResult placeItem(ItemStack stack, Item item, UseOnContext context, Operation<InteractionResult> original) {
        Level level = context.getLevel();
        if (level.isClientSide() || !AbyssEventBus.hasListeners(BlockEvent.EntityPlaceEvent.class)) {
            return original.call(item, context);
        }
        FabricBlockCaptureState capture = (FabricBlockCaptureState)level;
        Player player = context.getPlayer();
        int size = stack.getCount();
        DataComponentMap components = stack.getComponents();
        if (!(item instanceof BucketItem)) {
            capture.theabyss$setCapturing(true);
        }
        InteractionResult result;
        try {
            result = original.call(item, context);
        } finally {
            capture.theabyss$setCapturing(false);
        }
        List<FabricBlockSnapshot> snapshots = new ArrayList<>(capture.theabyss$snapshots());
        capture.theabyss$snapshots().clear();
        if (!result.consumesAction() || snapshots.isEmpty()) {
            return result;
        }
        int newSize = stack.getCount();
        DataComponentMap newComponents = stack.getComponents();
        stack.setCount(size);
        stack.applyComponents(components);
        Direction side = context.getClickedFace();
        FabricBlockSnapshot first = snapshots.getFirst();
        boolean canceled = AbyssEventBus.post(new BlockEvent.EntityPlaceEvent(player, level, first.pos())).isCanceled();
        if (canceled) {
            for (FabricBlockSnapshot snapshot : Lists.reverse(snapshots)) {
                capture.theabyss$setRestoring(true);
                try {
                    snapshot.restore(FabricBlockSnapshot.restoreFlags(snapshot));
                } finally {
                    capture.theabyss$setRestoring(false);
                }
            }
            if (player instanceof ServerPlayer serverPlayer) {
                int slot = context.getHand() == InteractionHand.MAIN_HAND ? serverPlayer.getInventory().getSelectedSlot() : Inventory.SLOT_OFFHAND;
                serverPlayer.connection.send(serverPlayer.getInventory().createInventoryUpdatePacket(slot));
            }
            return InteractionResult.FAIL;
        }
        stack.setCount(newSize);
        stack.applyComponents(newComponents);
        for (FabricBlockSnapshot snapshot : snapshots) {
            BlockState oldState = snapshot.state();
            BlockState newState = level.getBlockState(snapshot.pos());
            newState.onPlace(level, snapshot.pos(), oldState, false);
            notifyBlock(level, snapshot.pos(), oldState, newState, snapshot.flags(), 512);
        }
        return result;
    }

    public static void notifyBlock(Level level, BlockPos pos, BlockState oldState, BlockState blockState, int flags, int updateLimit) {
        LevelChunk chunk = level.getChunkAt(pos);
        Block block = blockState.getBlock();
        BlockState newState = level.getBlockState(pos);
        if (newState != blockState) {
            return;
        }
        if (oldState != newState) {
            level.setBlocksDirty(pos, oldState, newState);
        }
        if ((flags & Block.UPDATE_CLIENTS) != 0 && (!level.isClientSide() || (flags & Block.UPDATE_INVISIBLE) == 0)
            && (level.isClientSide() || chunk.getFullStatus() != null && chunk.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING))) {
            level.sendBlockUpdated(pos, oldState, blockState, flags);
        }
        if ((flags & Block.UPDATE_NEIGHBORS) != 0) {
            level.updateNeighborsAt(pos, oldState.getBlock());
            if (!level.isClientSide() && blockState.hasAnalogOutputSignal()) {
                level.updateNeighbourForOutputSignal(pos, block);
            }
        }
        if ((flags & Block.UPDATE_KNOWN_SHAPE) == 0 && updateLimit > 0) {
            int neighbourFlags = flags & ~(Block.UPDATE_NEIGHBORS | Block.UPDATE_SUPPRESS_DROPS);
            oldState.updateIndirectNeighbourShapes(level, pos, neighbourFlags, updateLimit - 1);
            blockState.updateNeighbourShapes(level, pos, neighbourFlags, updateLimit - 1);
            blockState.updateIndirectNeighbourShapes(level, pos, neighbourFlags, updateLimit - 1);
        }
        level.updatePOIOnBlockStateChange(pos, oldState, newState);
    }

    public static boolean isLevelAccessorCapturing(LevelAccessor level) {
        return level instanceof Level real && isCapturing(real);
    }
}
