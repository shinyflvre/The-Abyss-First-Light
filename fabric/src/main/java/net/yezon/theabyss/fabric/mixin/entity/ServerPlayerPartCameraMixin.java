package net.yezon.theabyss.fabric.mixin.entity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.fabric.entity.FabricPartEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerPartCameraMixin {
    @ModifyVariable(method = "setCamera", at = @At("HEAD"), argsOnly = true)
    private @Nullable Entity theabyss$partCamera(@Nullable Entity camera) {
        Entity result = camera;
        while (result instanceof FabricPartEntity part) {
            result = part.getParent();
        }
        return result;
    }
}
