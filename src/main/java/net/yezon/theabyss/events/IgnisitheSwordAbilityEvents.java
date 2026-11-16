package net.yezon.theabyss.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public class IgnisitheSwordAbilityEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity != null) {
            if (!MagicianSpawnEvents.isRaining(world)) {
                entity.igniteForSeconds(15);
            }

            if (MagicianSpawnEvents.isRaining(world) && Math.random() < 0.3) {
                if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                    _entity.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 450, 1));
                }

                if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                    _entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 1));
                }

                entity.igniteForSeconds(15);
                if (world instanceof ServerLevel _level) {
                    LightningBolt entityToSpawn = EntityTypes.LIGHTNING_BOLT.create(_level, EntitySpawnReason.TRIGGERED);
                    entityToSpawn.snapTo(Vec3.atBottomCenterOf(BlockPos.containing(x, y, z)));
                    entityToSpawn.setVisualOnly(true);
                    _level.addFreshEntity(entityToSpawn);
                }
            }
        }
    }
}
