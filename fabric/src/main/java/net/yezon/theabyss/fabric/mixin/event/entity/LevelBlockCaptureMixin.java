package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.fabric.event.FabricBlockCaptureState;
import net.yezon.theabyss.fabric.event.FabricBlockSnapshot;
import net.yezon.theabyss.fabric.event.LevelFabricEvents;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Level.class)
public abstract class LevelBlockCaptureMixin implements FabricBlockCaptureState {
    @Unique
    private static final Object THEABYSS_NO_SNAPSHOT = new Object();

    @Unique
    private boolean theabyss$capture;

    @Unique
    private boolean theabyss$restore;

    @Unique
    private boolean theabyss$skipNotify;

    @Unique
    private final List<FabricBlockSnapshot> theabyss$captured = new ArrayList<>();

    @Unique
    private final ArrayDeque<Object> theabyss$pending = new ArrayDeque<>();

    @Override
    public boolean theabyss$capturing() {
        return this.theabyss$capture;
    }

    @Override
    public void theabyss$setCapturing(boolean capturing) {
        this.theabyss$capture = capturing;
    }

    @Override
    public boolean theabyss$restoring() {
        return this.theabyss$restore;
    }

    @Override
    public void theabyss$setRestoring(boolean restoring) {
        this.theabyss$restore = restoring;
    }

    @Override
    public List<FabricBlockSnapshot> theabyss$snapshots() {
        return this.theabyss$captured;
    }

    @ModifyArg(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/chunk/LevelChunk;setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;"),
        index = 0)
    private BlockPos theabyss$snapshotBlock(BlockPos pos, @Local(argsOnly = true, ordinal = 0) int flags) {
        if (this.theabyss$capture) {
            FabricBlockSnapshot snapshot = LevelFabricEvents.snapshotBeforeSet((Level)(Object)this, pos, flags);
            this.theabyss$pending.push(snapshot != null ? snapshot : THEABYSS_NO_SNAPSHOT);
        }
        return pos;
    }

    @ModifyExpressionValue(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/chunk/LevelChunk;setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;"))
    private @Nullable BlockState theabyss$capturedBlock(@Nullable BlockState old) {
        if (!this.theabyss$capture || this.theabyss$pending.isEmpty()) {
            return old;
        }
        if (this.theabyss$pending.pop() instanceof FabricBlockSnapshot snapshot) {
            if (old == null) {
                LevelFabricEvents.dropSnapshot((Level)(Object)this, snapshot);
            } else {
                this.theabyss$skipNotify = true;
            }
        }
        return old;
    }

    @ModifyExpressionValue(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0))
    private @Nullable BlockState theabyss$skipCapturedNotify(BlockState current) {
        if (this.theabyss$skipNotify) {
            this.theabyss$skipNotify = false;
            return null;
        }
        return current;
    }
}
