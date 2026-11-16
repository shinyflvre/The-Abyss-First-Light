package net.yezon.theabyss.events;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public class GuardSpawnEffectEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null) {
            return;
        }

        entity.setCustomName(Component.literal(entity.getDisplayName().getString()));
        if (Math.random() < 0.08 && world instanceof ServerLevel _level) {
            LightningBolt entityToSpawn = EntityTypes.LIGHTNING_BOLT.create(_level, EntitySpawnReason.TRIGGERED);
            if (entityToSpawn != null) {
                entityToSpawn.snapTo(Vec3.atBottomCenterOf(BlockPos.containing(x, y, z)));
                entityToSpawn.setVisualOnly(true);
                _level.addFreshEntity(entityToSpawn);
            }
        }
    }
}
