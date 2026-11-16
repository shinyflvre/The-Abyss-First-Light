package net.yezon.theabyss.events;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class GhostPotionEndEvents {
    public static void execute(Entity entity) {
        if (entity == null) {
            return;
        }
        if (entity instanceof ServerPlayer player) {
            CompoundTag data = player.getPersistentData();
            GameType previous = GameType.byName(data.getStringOr(GhostPotionStartEvents.SAVED_MODE, "survival"), GameType.SURVIVAL);
            data.remove(GhostPotionStartEvents.SAVED_MODE);
            player.setGameMode(previous);
            if (player.isAlive() && player.level() instanceof ServerLevel level) {
                Vec3 free = freeSpot(level, player);
                if (free != null) {
                    player.teleportTo(free.x, free.y, free.z);
                    player.connection.teleport(free.x, free.y, free.z, player.getYRot(), player.getXRot());
                }
                player.resetFallDistance();
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, false));
            }
        }
        if (entity instanceof LivingEntity living && !living.level().isClientSide()) {
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 1, false, false));
            living.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 250, 3, false, false));
        }
    }

    private static Vec3 freeSpot(ServerLevel level, ServerPlayer player) {
        AABB box = player.getBoundingBox();
        if (level.noCollision(player, box)) {
            return null;
        }
        Vec3 pos = player.position();
        BlockPos base = player.blockPosition();
        for (int up = 0; up <= 48; up++) {
            Vec3 at = new Vec3(pos.x, base.getY() + up, pos.z);
            if (level.noCollision(player, box.move(at.subtract(pos)))) {
                return at;
            }
        }
        for (int r = 1; r <= 6; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    for (int dy = -2; dy <= 4; dy++) {
                        Vec3 at = new Vec3(base.getX() + dx + 0.5, base.getY() + dy, base.getZ() + dz + 0.5);
                        if (level.noCollision(player, box.move(at.subtract(pos)))) {
                            return at;
                        }
                    }
                }
            }
        }
        return null;
    }
}
