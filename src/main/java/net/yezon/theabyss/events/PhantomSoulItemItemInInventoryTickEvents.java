package net.yezon.theabyss.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;

public final class PhantomSoulItemItemInInventoryTickEvents {
    private static final int UNLUCK_LEVEL = 1;
    private static final int UNLUCK_TICKS = 20;
    private static final int REFRESH_BELOW = 5;

    private PhantomSoulItemItemInInventoryTickEvents() {
    }

    public static void execute(Entity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        MobEffectInstance unluck = player.getEffect(MobEffects.UNLUCK);
        if (unluck == null || unluck.getAmplifier() < UNLUCK_LEVEL || unluck.getAmplifier() == UNLUCK_LEVEL && unluck.getDuration() <= REFRESH_BELOW) {
            player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, UNLUCK_TICKS, UNLUCK_LEVEL));
        }
        TheAbyssAdvGrantEvents.grant(player, "theabyss:ore_adv");
        TheAbyssAdvGrantEvents.grant(player, "theabyss:phantom_ingot_adv");
    }
}
