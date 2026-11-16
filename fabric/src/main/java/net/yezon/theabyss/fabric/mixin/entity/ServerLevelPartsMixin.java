package net.yezon.theabyss.fabric.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.fabric.entity.FabricParts;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
public abstract class ServerLevelPartsMixin {
    @ModifyReturnValue(method = "getEntityOrPart", at = @At("RETURN"))
    private @Nullable Entity theabyss$findPart(@Nullable Entity original, @Local(argsOnly = true) int id) {
        return original != null ? original : FabricParts.part((ServerLevel)(Object)this, id);
    }
}
