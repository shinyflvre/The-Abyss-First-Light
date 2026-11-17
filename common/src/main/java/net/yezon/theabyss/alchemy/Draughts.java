package net.yezon.theabyss.alchemy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.living.LivingDamageEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerEvent;
import net.yezon.theabyss.platform.event.tick.PlayerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModMobEffects;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.SomniumUpgrades;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class Draughts {
    public static final double WARD_MAX = 20.0;
    public static final int WARD_TICKS = 2400;
    public static final int STILLNESS_TICKS = 3600;
    public static final int SHARED_HEART_TICKS = 1200;
    public static final float SHARED_PART = 0.5F;
    private static final double STILLNESS_FACTOR = 3.0;
    private static final double SOMNIUM_MAX = 100.0;
    private static final double STILL_EPSILON = 1.0E-4;
    private static final Map<UUID, Vec3> STILL = new HashMap<>();

    private Draughts() {
    }

    public static void drinkWard(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        entity.addEffect(new MobEffectInstance(TheabyssModMobEffects.SOMNIUM_WARD.getDelegate(), WARD_TICKS, 0));
        if (entity instanceof Player player) {
            TheabyssModVariables.updatePlayerVariables(player, vars -> {
                vars.SomniumWard = Math.min(WARD_MAX, player.getMaxHealth());
                vars.syncPlayerVariables(player);
            });
        }
    }

    public static void drinkStillness(LivingEntity entity) {
        if (!entity.level().isClientSide()) {
            entity.addEffect(new MobEffectInstance(TheabyssModMobEffects.STILLNESS.getDelegate(), STILLNESS_TICKS, 0));
        }
    }

    public static void drinkSharedHeart(LivingEntity entity) {
        if (!entity.level().isClientSide()) {
            entity.addEffect(new MobEffectInstance(TheabyssModMobEffects.SHARED_HEART.getDelegate(), SHARED_HEART_TICKS, 0));
        }
    }

    public static void endWard(LivingEntity entity) {
        if (entity instanceof Player player) {
            TheabyssModVariables.updatePlayerVariables(player, vars -> {
                vars.SomniumWard = 0.0;
                vars.syncPlayerVariables(player);
            });
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player) || !player.hasEffect(TheabyssModMobEffects.SOMNIUM_WARD.getDelegate())) {
            return;
        }
        float damage = event.getNewDamage();
        if (damage <= 0.0F) {
            return;
        }
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
        if (vars.SomniumWard <= 0.0) {
            return;
        }
        float absorbed = (float)Math.min(vars.SomniumWard, damage);
        vars.SomniumWard = Math.max(0.0, vars.SomniumWard - absorbed);
        vars.syncPlayerVariables(player);
        event.setNewDamage(damage - absorbed);
    }

    @SubscribeEvent
    public static void onHurt(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel level) || !target.hasEffect(TheabyssModMobEffects.SHARED_HEART.getDelegate())) {
            return;
        }
        DamageSource source = event.getSource();
        if (source.is(DamageTypes.THORNS) || !(source.getEntity() instanceof LivingEntity attacker) || attacker == target || !attacker.isAlive()) {
            return;
        }
        float back = (event.getOriginalDamage() - event.getBlockedDamage()) * SHARED_PART;
        if (back > 0.0F) {
            attacker.hurt(level.damageSources().thorns(target), back);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        UUID id = player.getUUID();
        if (!player.hasEffect(TheabyssModMobEffects.STILLNESS.getDelegate())) {
            STILL.remove(id);
            return;
        }
        Vec3 pos = player.position();
        Vec3 last = STILL.put(id, pos);
        if (last == null || last.distanceToSqr(pos) > STILL_EPSILON || player.getHealth() < player.getMaxHealth()) {
            return;
        }
        TheabyssModVariables.updatePlayerVariables(player, vars -> {
            if (vars.Mana < SOMNIUM_MAX) {
                double level = Math.max(1.0, vars.ManaRegenUpgrade);
                double extra = STILLNESS_FACTOR * SomniumUpgrades.regenPerTick(level) - SomniumUpgrades.regenPerTick(vars.ManaRegenUpgrade);
                vars.Mana = Math.min(SOMNIUM_MAX, vars.Mana + extra);
            }
        });
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        STILL.remove(event.getEntity().getUUID());
    }
}
