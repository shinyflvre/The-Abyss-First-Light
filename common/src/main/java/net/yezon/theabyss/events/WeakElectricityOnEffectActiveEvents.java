package net.yezon.theabyss.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.yezon.theabyss.init.TheabyssModParticleTypes;

public class WeakElectricityOnEffectActiveEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity != null) {
            if (world instanceof ServerLevel _level) {
                _level.sendParticles(TheabyssModParticleTypes.SHOCK_FX.get(), x, y, z, 10, 0.1, 1.0, 0.1, 0.02);
            }

            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                _entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 1, false, false));
            }
        }
    }
}
