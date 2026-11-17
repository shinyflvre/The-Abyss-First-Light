package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.level.ServerLevelAccessor;
import net.yezon.theabyss.fabric.event.EntityFabricEvents;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Zombie.class)
public abstract class FinalizeSpawnZombieMixin {
    @WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Zombie;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;"))
    private SpawnGroupData theabyss$finalizeSpawn0(Zombie mob, ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
        @Nullable SpawnGroupData data, Operation<SpawnGroupData> original) {
        return EntityFabricEvents.finalizeSpawn(mob, level, difficulty, reason, data, original);
    }

    @WrapOperation(method = "finalizeSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Chicken;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;"))
    private SpawnGroupData theabyss$finalizeSpawn1(Chicken mob, ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
        @Nullable SpawnGroupData data, Operation<SpawnGroupData> original) {
        return EntityFabricEvents.finalizeSpawn(mob, level, difficulty, reason, data, original);
    }

    @WrapOperation(method = "killedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/ZombieVillager;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;"))
    private SpawnGroupData theabyss$finalizeSpawn2(ZombieVillager mob, ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
        @Nullable SpawnGroupData data, Operation<SpawnGroupData> original) {
        return EntityFabricEvents.finalizeSpawn(mob, level, difficulty, reason, data, original);
    }
}
