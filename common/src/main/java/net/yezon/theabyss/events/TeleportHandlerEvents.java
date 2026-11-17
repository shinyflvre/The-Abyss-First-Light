package net.yezon.theabyss.events;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.ring.RingVisuals;

public class TeleportHandlerEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null || !(world instanceof ServerLevel level)) {
            return;
        }
        Vec3 from = entity.position();
        if (entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 24, 1));
            living.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 1));
        }
        net.yezon.theabyss.util.AbyssTeleport.to(entity, x, y, z);
        if (entity instanceof ServerPlayer player) {
            net.yezon.theabyss.util.AbyssTeleport.connection(player, x, y, z, entity.getYRot(), entity.getXRot());
        }
        RingVisuals.teleportDepart(level, from, entity.getBbHeight());
        RingVisuals.teleportArrive(level, new Vec3(x, y, z));
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:spell_of_teleport"));
        if (sound != null) {
            level.playSound(null, x, y, z, sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
    }
}
