package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.camel.CamelHusk;
import net.minecraft.world.entity.monster.skeleton.Parched;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.level.ServerLevelAccessor;
import net.yezon.theabyss.fabric.event.EntityFabricEvents;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Husk.class)
public abstract class FinalizeSpawnHuskMixin {
    @WrapOperation(method = "finalizeSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/camel/CamelHusk;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;"))
    private SpawnGroupData theabyss$finalizeSpawn0(CamelHusk mob, ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
        @Nullable SpawnGroupData data, Operation<SpawnGroupData> original) {
        return EntityFabricEvents.finalizeSpawn(mob, level, difficulty, reason, data, original);
    }

    @WrapOperation(method = "finalizeSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/skeleton/Parched;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;"))
    private SpawnGroupData theabyss$finalizeSpawn1(Parched mob, ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
        @Nullable SpawnGroupData data, Operation<SpawnGroupData> original) {
        return EntityFabricEvents.finalizeSpawn(mob, level, difficulty, reason, data, original);
    }
}
