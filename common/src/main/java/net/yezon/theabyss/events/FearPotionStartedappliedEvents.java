package net.yezon.theabyss.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.yezon.theabyss.init.TheabyssModMobEffects;

public class FearPotionStartedappliedEvents {
    public static final int DURATION = 800;
    private static final double SPREAD_RADIUS = 50.0;

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (!(entity instanceof ServerPlayer player) || !(world instanceof ServerLevel level)) {
            return;
        }
        player.sendOverlayMessage(Component.translatable("event.theabyss.fear"));
        for (ServerPlayer other : level.players()) {
            if (other != player && !other.isSpectator() && other.distanceToSqr(player) < SPREAD_RADIUS * SPREAD_RADIUS
                && !other.hasEffect(TheabyssModMobEffects.FEAR.getDelegate()) && !other.hasEffect(TheabyssModMobEffects.ANTI_FEAR_POTION.getDelegate())) {
                other.addEffect(new MobEffectInstance(TheabyssModMobEffects.FEAR.getDelegate(), DURATION, 0));
            }
        }
    }
}
