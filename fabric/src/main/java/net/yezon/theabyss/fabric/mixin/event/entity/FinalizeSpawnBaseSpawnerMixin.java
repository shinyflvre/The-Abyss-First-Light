package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.BaseSpawner;
import net.yezon.theabyss.fabric.event.EntityFabricEvents;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseSpawner.class)
public abstract class FinalizeSpawnBaseSpawnerMixin {
    @Unique
    private boolean theabyss$finalized;

    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/Mob;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;"))
    private SpawnGroupData theabyss$finalizeSpawn(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
        @Nullable SpawnGroupData data, Operation<SpawnGroupData> original) {
        this.theabyss$finalized = true;
        return EntityFabricEvents.finalizeSpawn(mob, level, difficulty, reason, data, original);
    }

    @Inject(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/SpawnData;getEquipment()Ljava/util/Optional;"))
    private void theabyss$finalizeSpawnWithoutDefaults(net.minecraft.server.level.ServerLevel serverLevel, net.minecraft.core.BlockPos pos, CallbackInfo ci, @Local(ordinal = 0) Mob mob) {
        if (!this.theabyss$finalized && EntityFabricEvents.listensToFinalizeSpawn()) {
            EntityFabricEvents.postFinalizeSpawn(mob, MobSpawnType.SPAWNER);
        }
        this.theabyss$finalized = false;
    }
}
