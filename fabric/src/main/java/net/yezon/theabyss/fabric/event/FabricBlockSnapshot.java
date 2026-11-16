package net.yezon.theabyss.fabric.event;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public final class FabricBlockSnapshot {
    private static final Logger LOGGER = LogUtils.getLogger();
    final Level level;
    final BlockPos pos;
    final BlockState state;
    final int flags;
    final @Nullable CompoundTag tag;

    private FabricBlockSnapshot(Level level, BlockPos pos, BlockState state, int flags, @Nullable CompoundTag tag) {
        this.level = level;
        this.pos = pos;
        this.state = state;
        this.flags = flags;
        this.tag = tag;
    }

    static FabricBlockSnapshot create(Level level, BlockPos pos, int flags) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        CompoundTag tag = blockEntity != null ? blockEntity.saveWithFullMetadata(level.registryAccess()) : null;
        return new FabricBlockSnapshot(level, pos.immutable(), level.getBlockState(pos), flags, tag);
    }

    void restore(int restoreFlags) {
        BlockState replaced = this.state;
        if (!this.level.setBlock(this.pos, replaced, restoreFlags)) {
            return;
        }
        this.level.sendBlockUpdated(this.pos, this.level.getBlockState(this.pos), replaced, restoreFlags);
        if (this.tag != null) {
            BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
            if (blockEntity != null) {
                try (ProblemReporter.ScopedCollector problems = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), LOGGER)) {
                    blockEntity.loadWithComponents(TagValueInput.create(problems, this.level.registryAccess(), this.tag));
                    blockEntity.setChanged();
                }
            }
        }
    }

    BlockState state() {
        return this.state;
    }

    BlockPos pos() {
        return this.pos;
    }

    int flags() {
        return this.flags;
    }

    static int restoreFlags(FabricBlockSnapshot snapshot) {
        return snapshot.flags | Block.UPDATE_CLIENTS;
    }
}
