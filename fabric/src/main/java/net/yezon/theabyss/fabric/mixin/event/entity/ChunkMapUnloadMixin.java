package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.LevelChunk;
import net.yezon.theabyss.fabric.event.LevelFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkMap.class)
public abstract class ChunkMapUnloadMixin {
    @WrapOperation(method = "method_60440", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;setLoaded(Z)V"))
    private void theabyss$chunkUnload(LevelChunk chunk, boolean loaded, Operation<Void> original) {
        original.call(chunk, loaded);
        LevelFabricEvents.chunkUnload(chunk);
    }
}
