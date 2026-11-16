package net.yezon.theabyss.ring.secondary;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TeleportDashMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.util.FallGuard;

public final class Swap {
    public static final double RANGE = 30.0;

    private Swap() {
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.level();
        LivingEntity target = Aim.creature(level, player, RANGE);
        if (target == null) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.secondary.no_target", Component.translatable("tooltip.theabyss.ring_of_teleport.secondary.ability")));
            return false;
        }
        Vec3 mine = player.position();
        Vec3 theirs = target.position();
        Vec3 myBody = mine.add(0.0, player.getBbHeight() * 0.5, 0.0);
        Vec3 theirBody = theirs.add(0.0, target.getBbHeight() * 0.5, 0.0);
        Vec3 fromEye = player.getEyePosition();
        float fromYaw = player.getYRot();
        float fromPitch = player.getXRot();
        Vec3 look = mine.add(0.0, target.getEyeHeight(), 0.0).subtract(theirs.add(0.0, player.getEyeHeight(), 0.0));
        float yaw = (float)(Mth.atan2(look.z, look.x) * Mth.RAD_TO_DEG) - 90.0F;
        float pitch = (float)(-(Mth.atan2(look.y, look.horizontalDistance()) * Mth.RAD_TO_DEG));
        SpellFxMessage.around(level, mine, SpellFxMessage.SWAP, List.of(player.getId(), target.getId()), SpellFxMessage.points(myBody, theirBody), 26);
        RingVisuals.teleportStepDepart(level, target);
        PacketDistributor.sendToPlayer(player, new TeleportDashMessage(fromEye.x, fromEye.y, fromEye.z, fromYaw, fromPitch));
        if (player.isPassenger()) {
            player.stopRiding();
        }
        if (target.isPassenger()) {
            target.stopRiding();
        }
        player.teleportTo(theirs.x, theirs.y, theirs.z);
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.setYHeadRot(yaw);
        player.connection.teleport(theirs.x, theirs.y, theirs.z, yaw, pitch);
        if (target instanceof ServerPlayer other) {
            other.connection.teleport(mine.x, mine.y, mine.z, other.getYRot(), other.getXRot());
        } else {
            target.teleportTo(mine.x, mine.y, mine.z);
        }
        player.resetFallDistance();
        target.resetFallDistance();
        FallGuard.protect(player, 60);
        FallGuard.protect(target, 60);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0, false, false));
        if (target instanceof Mob mob) {
            mob.getNavigation().stop();
        }
        RingVisuals.teleportStepArrive(level, mine);
        RingVisuals.sound(level, theirs, SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.6F, 0.8F);
        RingVisuals.sound(level, mine, SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.6F, 1.1F);
        RingVisuals.sound(level, theirs, SoundEvents.ENDERMAN_TELEPORT, 1.0F, 0.6F);
        var sound = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getValue(net.minecraft.resources.Identifier.parse("theabyss:spell_of_teleport"));
        if (sound != null) {
            RingVisuals.sound(level, theirs, sound, 1.0F, 1.2F);
        }
        return true;
    }
}
