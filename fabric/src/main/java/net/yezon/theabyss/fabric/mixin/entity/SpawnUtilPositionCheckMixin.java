package net.yezon.theabyss.fabric.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.yezon.theabyss.fabric.entity.FabricSpawnChecks;
import net.yezon.theabyss.platform.event.entity.living.MobSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpawnUtil.class)
public abstract class SpawnUtilPositionCheckMixin {
    @WrapOperation(method = "trySpawnMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;checkSpawnRules(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/MobSpawnType;)Z"))
    private static boolean theabyss$positionCheckRules(Mob mob, LevelAccessor level, MobSpawnType reason, Operation<Boolean> original,
        @Share("theabyssPositionCheck") LocalRef<MobSpawnEvent.PositionCheck.Result> shared) {
        return FabricSpawnChecks.checkSpawnRules(mob, level, reason, original, shared);
    }

    @WrapOperation(method = "trySpawnMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;checkSpawnObstruction(Lnet/minecraft/world/level/LevelReader;)Z"))
    private static boolean theabyss$positionCheckObstruction(Mob mob, LevelReader level, Operation<Boolean> original,
        @Share("theabyssPositionCheck") LocalRef<MobSpawnEvent.PositionCheck.Result> shared) {
        return FabricSpawnChecks.checkSpawnObstruction(mob, level, original, shared);
    }
}
