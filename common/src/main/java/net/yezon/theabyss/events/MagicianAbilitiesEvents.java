package net.yezon.theabyss.events;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.registries.BuiltInRegistries;
import net.yezon.theabyss.entity.MagicianEntity;

public class MagicianAbilitiesEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity != null) {
            if (Math.random() < 0.004) {
                Vec3 _center = new Vec3(x, y, z);

                for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(20.0), e -> true)
                    .stream()
                    .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                    .toList()) {
                    if (entityiterator != entity && entityiterator instanceof Player) {
                        entityiterator.igniteForSeconds(8);
                    }
                }

                if (world instanceof Level _level) {
                    if (!_level.isClientSide()) {
                        _level.playSound(
                            (Player)null,
                            BlockPos.containing(x, y, z),
                            BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:spell_of_fire")),
                            SoundSource.NEUTRAL,
                            0.8F,
                            1.0F
                        );
                    } else {
                        _level.playLocalSound(
                            x,
                            y,
                            z,
                            BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:spell_of_fire")),
                            SoundSource.NEUTRAL,
                            0.8F,
                            1.0F,
                            false
                        );
                    }
                }
            }

            if (Math.random() < 0.002) {
                Vec3 _center = new Vec3(x, y, z);

                for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(20.0), e -> true)
                    .stream()
                    .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                    .toList()) {
                    if (entityiterator != entity
                        && entityiterator instanceof Player
                        && entityiterator instanceof LivingEntity _entity
                        && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 5, false, false));
                    }
                }

                if (world instanceof Level _level) {
                    if (!_level.isClientSide()) {
                        _level.playSound(
                            (Player)null,
                            BlockPos.containing(x, y, z),
                            BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:spell_of_freeze")),
                            SoundSource.NEUTRAL,
                            0.8F,
                            1.0F
                        );
                    } else {
                        _level.playLocalSound(
                            x,
                            y,
                            z,
                            BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:spell_of_freeze")),
                            SoundSource.NEUTRAL,
                            0.8F,
                            1.0F,
                            false
                        );
                    }
                }
            }

            if (Math.random() < 0.002) {
                Vec3 _center = new Vec3(x, y, z);

                for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(20.0), e -> true)
                    .stream()
                    .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                    .toList()) {
                    if (entityiterator != entity
                        && entityiterator instanceof MagicianEntity
                        && entity instanceof LivingEntity _entity
                        && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 5, false, false));
                    }

                    if (entity instanceof MagicianEntity && entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 5, false, false));
                    }
                }

                if (world instanceof Level _level) {
                    if (!_level.isClientSide()) {
                        _level.playSound(
                            (Player)null,
                            BlockPos.containing(x, y, z),
                            BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:spell_of_invisibility")),
                            SoundSource.NEUTRAL,
                            0.8F,
                            1.0F
                        );
                    } else {
                        _level.playLocalSound(
                            x,
                            y,
                            z,
                            BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:spell_of_invisibility")),
                            SoundSource.NEUTRAL,
                            0.8F,
                            1.0F,
                            false
                        );
                    }
                }
            }

            if (Math.random() < 0.001) {
                Vec3 _center = new Vec3(x, y, z);

                for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(20.0), e -> true)
                    .stream()
                    .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                    .toList()) {
                    if (entityiterator != entity
                        && entityiterator instanceof Player
                        && entityiterator instanceof LivingEntity _entity
                        && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, false, false));
                    }
                }

                if (world instanceof Level _level) {
                    if (!_level.isClientSide()) {
                        _level.playSound(
                            (Player)null,
                            BlockPos.containing(x, y, z),
                            BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:spell_of_ghost")),
                            SoundSource.NEUTRAL,
                            0.8F,
                            1.0F
                        );
                    } else {
                        _level.playLocalSound(
                            x,
                            y,
                            z,
                            BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:spell_of_ghost")),
                            SoundSource.NEUTRAL,
                            0.8F,
                            1.0F,
                            false
                        );
                    }
                }
            }

            if (Math.random() < 0.006) {
                Vec3 _center = new Vec3(x, y, z);

                for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(20.0), e -> true)
                    .stream()
                    .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                    .toList()) {
                    if (entityiterator != entity && entityiterator instanceof Player) {
                        Entity _ent = entityiterator;
                        net.yezon.theabyss.util.AbyssTeleport.to(_ent, entity.getX(), entity.getY(), entity.getZ());
                        if (_ent instanceof ServerPlayer _serverPlayer) {
                            net.yezon.theabyss.util.AbyssTeleport.connection(_serverPlayer, entity.getX(), entity.getY(), entity.getZ(), _ent.getYRot(), _ent.getXRot());
                        }
                    }
                }

                if (world instanceof Level _level) {
                    if (!_level.isClientSide()) {
                        _level.playSound(
                            (Player)null,
                            BlockPos.containing(x, y, z),
                            BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:spell_of_teleport")),
                            SoundSource.NEUTRAL,
                            0.2F,
                            1.0F
                        );
                    } else {
                        _level.playLocalSound(
                            x,
                            y,
                            z,
                            BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:spell_of_teleport")),
                            SoundSource.NEUTRAL,
                            0.2F,
                            1.0F,
                            false
                        );
                    }
                }
            }
        }
    }
}
