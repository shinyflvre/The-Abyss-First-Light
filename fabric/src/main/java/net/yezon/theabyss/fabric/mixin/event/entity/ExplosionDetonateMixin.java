package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerExplosion;
import net.yezon.theabyss.fabric.event.LevelFabricEvents;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerExplosion.class)
public abstract class ExplosionDetonateMixin {
    @Shadow
    @Final
    private ServerLevel level;

    @Unique
    private @Nullable List<BlockPos> theabyss$toBlow;

    @WrapOperation(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerExplosion;hurtEntities()V"))
    private void theabyss$rememberBlocks(ServerExplosion explosion, Operation<Void> original, @Local List<BlockPos> toBlow) {
        this.theabyss$toBlow = toBlow;
        try {
            original.call(explosion);
        } finally {
            this.theabyss$toBlow = null;
        }
    }

    @ModifyExpressionValue(method = "hurtEntities", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/ServerLevel;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
    private List<Entity> theabyss$detonate(List<Entity> entities) {
        LevelFabricEvents.explosionDetonate(this.level, this.theabyss$toBlow);
        return entities;
    }
}
