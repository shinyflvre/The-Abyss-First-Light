package net.yezon.theabyss.fabric.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.event.entity.living.MobSpawnEvent;

public final class FabricSpawnChecks {
    private FabricSpawnChecks() {
    }

    public static boolean checkSpawnRules(Mob mob, LevelAccessor level, EntitySpawnReason reason, Operation<Boolean> original,
        LocalRef<MobSpawnEvent.PositionCheck.Result> shared) {
        MobSpawnEvent.PositionCheck.Result result = MobSpawnEvent.PositionCheck.Result.DEFAULT;
        if (level instanceof ServerLevelAccessor serverLevel && AbyssEventBus.hasListeners(MobSpawnEvent.PositionCheck.class)) {
            result = AbyssEventBus.post(new MobSpawnEvent.PositionCheck(mob, serverLevel, reason, MobSpawnEvent.PositionCheck.Result.DEFAULT)).getResult();
        }
        shared.set(result);
        if (result == MobSpawnEvent.PositionCheck.Result.DEFAULT) {
            return original.call(mob, level, reason);
        }
        return result == MobSpawnEvent.PositionCheck.Result.SUCCEED;
    }

    public static boolean checkSpawnObstruction(Mob mob, LevelReader level, Operation<Boolean> original, LocalRef<MobSpawnEvent.PositionCheck.Result> shared) {
        MobSpawnEvent.PositionCheck.Result result = shared.get();
        if (result == null || result == MobSpawnEvent.PositionCheck.Result.DEFAULT) {
            return original.call(mob, level);
        }
        return result == MobSpawnEvent.PositionCheck.Result.SUCCEED;
    }
}
