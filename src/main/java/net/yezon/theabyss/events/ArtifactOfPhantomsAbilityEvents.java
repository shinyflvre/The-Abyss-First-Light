package net.yezon.theabyss.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModParticleTypes;

public class ArtifactOfPhantomsAbilityEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null) {
            return;
        }

        if (entity instanceof Player _player) {
            _player.getCooldowns().addCooldown(new ItemStack(TheabyssModItems.ARTIFACT_OF_PHANTOMS.get()), 30);
        }

        if (world instanceof ServerLevel _level) {
            _level.sendParticles(TheabyssModParticleTypes.PHANTOM_FX.get(), x, y, z, 4, 0.005, 0.005, 0.005, 0.1);
            _level.sendParticles(TheabyssModParticleTypes.PHANTOM_FX.get(), x, y, z, 2, 0.002, 0.002, 0.002, 0.2);
        }

        if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
            if (Math.random() < 0.2) {
                _entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1, false, false));
            }

            if (Math.random() < 0.1) {
                _entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 1, false, false));
            }

            if (Math.random() < 0.03) {
                _entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 1, false, false));
            }

            if (Math.random() < 0.2) {
                _entity.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 100, 1, false, false));
            }

            if (Math.random() < 0.03) {
                _entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1, false, false));
            }
        }
    }
}
