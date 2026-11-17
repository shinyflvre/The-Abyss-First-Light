package net.yezon.theabyss.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public class ThunderFlowerEffectEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity != null) {
            if (entity instanceof Player && Math.random() < 0.4) {
                if (world instanceof ServerLevel _level) {
                    LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level);
                    entityToSpawn.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(x, y, z)));
                    entityToSpawn.setVisualOnly(true);
                    _level.addFreshEntity(entityToSpawn);
                }

                entity.hurt(new DamageSource(world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.MAGIC)), 4.0F);
            }
        }
    }
}
