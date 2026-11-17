package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.yezon.theabyss.fabric.event.LevelFabricEvents;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelChunk.class)
public abstract class LevelChunkCaptureMixin {
    @Shadow
    @Final
    private Level level;

    @ModifyExpressionValue(method = "setBlockState", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;isClientSide:Z", opcode = Opcodes.GETFIELD, ordinal = 1))
    private boolean theabyss$deferPlace(boolean clientSide) {
        return clientSide || LevelFabricEvents.isCapturing(this.level);
    }
}
