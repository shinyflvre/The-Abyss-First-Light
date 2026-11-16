package net.yezon.theabyss.events;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModMobEffects;
import net.yezon.theabyss.init.TheabyssModSounds;

public final class UseDashEvents {
    private static final int COOLDOWN = 50;
    private static final double SPEED = 1.5;
    private static final float SOUND_VOLUME = 0.75F;

    private UseDashEvents() {
    }

    public static void execute(Entity entity) {
        if (entity == null) {
            return;
        }
        if (entity instanceof LivingEntity living && !living.level().isClientSide()) {
            if (entity.onGround()) {
                living.addEffect(new MobEffectInstance(MobEffects.SPEED, 14, 2, false, false));
                living.addEffect(new MobEffectInstance(TheabyssModMobEffects.DASH, 20, 0, false, false));
            } else {
                living.addEffect(new MobEffectInstance(MobEffects.SPEED, 50, 2, false, false));
                living.addEffect(new MobEffectInstance(TheabyssModMobEffects.FLYING, 50, 2, false, false));
                living.addEffect(new MobEffectInstance(TheabyssModMobEffects.DASH, 40, 0, false, false));
            }
        }
        if (entity instanceof ServerPlayer player) {
            playToSelf(player, TheabyssModSounds.DASH);
            playToSelf(player, TheabyssModSounds.NIGHTBLADE_ATTACK);
        }
        if (entity instanceof LivingEntity living) {
            living.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
        }
        entity.setDeltaMovement(new Vec3(
            Math.sin(Math.toRadians(entity.getYRot() + 180.0F)) * SPEED,
            Math.sin(Math.toRadians(0.0F - entity.getXRot())),
            Math.cos(Math.toRadians(entity.getYRot())) * SPEED
        ));
        if (entity instanceof Player player) {
            player.getCooldowns().addCooldown(new ItemStack(TheabyssModItems.ABYSS_SWORD.get()), COOLDOWN);
        }
    }

    private static void playToSelf(ServerPlayer player, Holder<SoundEvent> sound) {
        player.connection.send(new ClientboundSoundPacket(sound, SoundSource.MASTER, player.getX(), player.getY(), player.getZ(), SOUND_VOLUME, 1.0F,
            player.level().getRandom().nextLong()));
    }
}
