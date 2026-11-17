package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.yezon.theabyss.fabric.event.EntityFabricEvents;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PatrolSpawner.class)
public abstract class FinalizeSpawnPatrolMixin {
    @WrapOperation(method = "spawnPatrolMember", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/PatrollingMonster;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;"))
    private SpawnGroupData theabyss$finalizeSpawn(PatrollingMonster mob, ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
        @Nullable SpawnGroupData data, Operation<SpawnGroupData> original) {
        return EntityFabricEvents.finalizeSpawn(mob, level, difficulty, reason, data, original);
    }
}
