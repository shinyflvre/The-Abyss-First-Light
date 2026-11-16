package net.yezon.theabyss.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.yezon.theabyss.init.TheabyssModMobEffects;
import net.yezon.theabyss.init.TheabyssModParticleTypes;

public class ElectricityOnEffectActiveEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null) {
            return;
        }

        if (world instanceof ServerLevel _level) {
            _level.sendParticles(TheabyssModParticleTypes.SHOCK_FX.get(), x, y, z, 2, 0.1, 1.0, 0.1, 0.02);
        }

        if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
            _entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 1, false, false));
        }

        int _amplifier = entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(TheabyssModMobEffects.ELECTRICITY)
            ? _livEnt.getEffect(TheabyssModMobEffects.ELECTRICITY).getAmplifier()
            : 0;
        int _armor = entity instanceof LivingEntity _livEnt ? _livEnt.getArmorValue() : 0;
        entity.hurtOrSimulate(
            new DamageSource(world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.MAGIC)),
            (float)(1 + _amplifier - _armor / 6.5)
        );
        if (Math.random() < 0.05 && world instanceof Level _level) {
            SoundEvent _sound = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:spell_of_elec"));
            if (!_level.isClientSide()) {
                _level.playSound((Player)null, BlockPos.containing(x, y, z), _sound, SoundSource.HOSTILE, 1.2F, 1.0F);
            } else {
                _level.playLocalSound(x, y, z, _sound, SoundSource.HOSTILE, 1.2F, 1.0F, false);
            }
        }
    }
}
