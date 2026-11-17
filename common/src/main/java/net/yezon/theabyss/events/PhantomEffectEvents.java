package net.yezon.theabyss.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.yezon.theabyss.init.TheabyssModParticleTypes;

public class PhantomEffectEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity != null) {
            if (world instanceof ServerLevel _level) {
                _level.sendParticles(TheabyssModParticleTypes.PHANTOM_FX.get(), x, y, z, 4, 0.005, 0.005, 0.005, 0.1);
            }

            if (world instanceof ServerLevel _level) {
                _level.sendParticles(TheabyssModParticleTypes.PHANTOM_FX.get(), x, y, z, 2, 0.002, 0.002, 0.002, 0.2);
            }

            if (Math.random() < 0.2 && entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, false));
            }

            if (Math.random() < 0.1 && entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                _entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 1, false, false));
            }

            if (Math.random() < 0.03 && entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                _entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 1, false, false));
            }

            if (Math.random() < 0.2 && entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                _entity.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 100, 1, false, false));
            }

            if (Math.random() < 0.03 && entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                _entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1, false, false));
            }
        }
    }
}
