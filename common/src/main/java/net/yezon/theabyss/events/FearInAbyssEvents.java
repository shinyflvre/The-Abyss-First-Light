package net.yezon.theabyss.events;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.tick.PlayerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModMobEffects;
import net.yezon.theabyss.network.TheabyssModVariables;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class FearInAbyssEvents {
    private static final ResourceKey<Level> THE_ABYSS = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("theabyss:the_abyss"));
    private static final double CHANCE_PER_TICK = 9.5E-5;

    private FearInAbyssEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().dimension() != THE_ABYSS) {
            return;
        }
        GameType mode = player.gameMode.getGameModeForPlayer();
        if ((mode == GameType.SURVIVAL || mode == GameType.ADVENTURE)
            && TheabyssModVariables.getPlayerVariables(player).FearEffect
            && !player.hasEffect(TheabyssModMobEffects.ANTI_FEAR_POTION.getDelegate())
            && !player.hasEffect(TheabyssModMobEffects.FEAR.getDelegate())
            && player.getRandom().nextDouble() < CHANCE_PER_TICK) {
            player.addEffect(new MobEffectInstance(TheabyssModMobEffects.FEAR.getDelegate(), FearPotionStartedappliedEvents.DURATION, 0));
        }
    }
}
