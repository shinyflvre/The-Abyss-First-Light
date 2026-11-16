package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.yezon.theabyss.fabric.event.LevelFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkMap.class)
public abstract class ChunkMapUnloadMixin {
    @WrapOperation(method = "lambda$scheduleUnload$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;unload(Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    private void theabyss$chunkUnload(ServerLevel level, LevelChunk chunk, Operation<Void> original) {
        LevelFabricEvents.chunkUnload(chunk);
        original.call(level, chunk);
    }
}
