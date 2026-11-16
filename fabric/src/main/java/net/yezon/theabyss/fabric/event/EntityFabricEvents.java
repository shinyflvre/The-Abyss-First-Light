package net.yezon.theabyss.fabric.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.HitResult;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.event.entity.EntityJoinLevelEvent;
import net.yezon.theabyss.platform.event.entity.ProjectileImpactEvent;
import net.yezon.theabyss.platform.event.entity.living.FinalizeSpawnEvent;
import org.jspecify.annotations.Nullable;

public final class EntityFabricEvents {
    private EntityFabricEvents() {
    }

    public static void register() {
    }

    public static void registerClient() {
    }

    public static boolean joinLevel(Entity entity, Level level, boolean loadedFromDisk) {
        boolean blocked = entity instanceof Mob mob && isSpawnCancelled(mob);
        if (!AbyssEventBus.hasListeners(EntityJoinLevelEvent.class)) {
            return blocked;
        }
        EntityJoinLevelEvent event = new EntityJoinLevelEvent(entity, level, loadedFromDisk);
        if (blocked) {
            event.setCanceled(true);
        }
        return AbyssEventBus.post(event).isCanceled();
    }

    public static boolean projectileImpact(Projectile projectile, HitResult hit) {
        if (hit.getType() == HitResult.Type.MISS || !AbyssEventBus.hasListeners(ProjectileImpactEvent.class)) {
            return false;
        }
        return AbyssEventBus.post(new ProjectileImpactEvent(projectile, hit)).isCanceled();
    }

    public static boolean isSpawnCancelled(Mob mob) {
        return ((FabricMobSpawnState)mob).theabyss$isSpawnCancelled();
    }

    public static @Nullable SpawnGroupData finalizeSpawn(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
        @Nullable SpawnGroupData data, Operation<SpawnGroupData> original) {
        if (!AbyssEventBus.hasListeners(FinalizeSpawnEvent.class)) {
            return original.call(mob, level, difficulty, reason, data);
        }
        if (!postFinalizeSpawn(mob, reason)) {
            return original.call(mob, level, difficulty, reason, data);
        }
        return null;
    }

    public static boolean listensToFinalizeSpawn() {
        return AbyssEventBus.hasListeners(FinalizeSpawnEvent.class);
    }

    public static boolean postFinalizeSpawn(Mob mob, EntitySpawnReason reason) {
        FinalizeSpawnEvent event = new FinalizeSpawnEvent(mob, reason, isSpawnCancelled(mob));
        AbyssEventBus.post(event);
        ((FabricMobSpawnState)mob).theabyss$setSpawnCancelled(event.getSpawnCancelled());
        return event.isCanceled();
    }
}
