package net.yezon.theabyss.enchant;

import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.living.LivingDamageEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingDeathEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.fx.RingFx;
import net.yezon.theabyss.util.RingMode;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class MeleeEnchantments {
    private static final float ECHO_CHANCE = 0.25F;
    private static final int ECHO_DELAY = 6;
    private static final int SHATTER_GLOW = 0x5FF5F0;
    private static final Set<UUID> ECHOING = new HashSet<>();

    private MeleeEnchantments() {
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel level) || ECHOING.contains(target.getUUID())) {
            return;
        }
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof Player player) || source.getDirectEntity() != player) {
            return;
        }
        if (AbyssEnchantments.level(level, AbyssEnchantments.PHANTOM_VENOM, player.getMainHandItem()) > 0) {
            int venomTarget = target.getId();
            TheabyssMod.queueServerWork(1, () -> venom(level, venomTarget));
        }
        if (AbyssEnchantments.level(level, AbyssEnchantments.NIGHTBLADE_ECHO, player.getMainHandItem()) <= 0 || level.getRandom().nextFloat() >= ECHO_CHANCE) {
            return;
        }
        float amount = event.getInflictedDamage() * 0.5F;
        if (amount <= 0.0F) {
            return;
        }
        UUID playerId = player.getUUID();
        int targetId = target.getId();
        Vec3 at = target.position().add(0.0, target.getBbHeight() * 0.55, 0.0);
        SpellFxMessage.around(level, at, SpellFxMessage.ENCHANT_ECHO, List.of(targetId), SpellFxMessage.points(player.getEyePosition(), at), ECHO_DELAY);
        TheabyssMod.queueServerWork(ECHO_DELAY, () -> echo(level, playerId, targetId, amount));
    }

    private static void venom(ServerLevel level, int targetId) {
        if (!(level.getEntity(targetId) instanceof LivingEntity target) || !target.isAlive()) {
            return;
        }
        MobEffectInstance poison = target.getEffect(MobEffects.POISON);
        if (poison == null || poison.getDuration() < 96) {
            return;
        }
        Vec3 at = target.position().add(0.0, target.getBbHeight() * 0.55, 0.0);
        SpellFxMessage.around(level, at, SpellFxMessage.PHANTOM_SPLASH, List.of(-1), SpellFxMessage.points(at), 0);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.PLAYERS, 0.8F, 0.7F);
    }

    private static void echo(ServerLevel level, UUID playerId, int targetId, float amount) {
        Entity entity = level.getEntity(targetId);
        Player player = level.getPlayerByUUID(playerId);
        if (!(entity instanceof LivingEntity target) || !target.isAlive() || player == null || target.level() != level) {
            return;
        }
        Vec3 at = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
        level.sendParticles(ParticleTypes.SQUID_INK, at.x, at.y, at.z, 6, 0.25, 0.3, 0.25, 0.03);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, 0.6F);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.35F, 1.7F);
        ECHOING.add(target.getUUID());
        try {
            target.invulnerableTime = 0;
            target.hurt(level.damageSources().indirectMagic(player, player), amount);
        } finally {
            ECHOING.remove(target.getUUID());
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (!(dead.level() instanceof ServerLevel level) || !(event.getSource().getEntity() instanceof Player player)
            || event.getSource().getDirectEntity() != player) {
            return;
        }
        int power = AbyssEnchantments.level(level, AbyssEnchantments.CRYSTAL_SHATTER, player.getMainHandItem());
        if (power <= 0) {
            return;
        }
        Vec3 at = dead.position().add(0.0, dead.getBbHeight() * 0.5, 0.0);
        double radius = 2.0 + power;
        float damage = 2.0F + 2.0F * power;
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(TheabyssModItems.ABYSS_CRYSTAL_SHARD.get())), at.x, at.y, at.z, 16, 0.25, 0.25, 0.25, 0.3);
        SpellFxMessage.around(level, at, SpellFxMessage.ENCHANT_SHATTER, List.of(), List.of(at.x, at.y, at.z, radius), 0);
        RingFx.wave(level, at, (float)radius + 1.0F, 12, SHATTER_GLOW);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 0.55F);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 0.8F, 0.7F);
        UUID playerId = player.getUUID();
        int deadId = dead.getId();
        TheabyssMod.queueServerWork(1, () -> shatter(level, playerId, deadId, at, radius, damage));
    }

    private static void shatter(ServerLevel level, UUID playerId, int deadId, Vec3 at, double radius, float damage) {
        Player player = level.getPlayerByUUID(playerId);
        if (player == null) {
            return;
        }
        List<LivingEntity> hits = new ArrayList<>();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(radius))) {
            if (e != player && e.getId() != deadId && e.isAlive() && !e.isSpectator() && e.distanceToSqr(at) <= radius * radius
                && !RingMode.spares(player, e)) {
                hits.add(e);
            }
        }
        for (LivingEntity e : hits) {
            e.hurt(level.damageSources().indirectMagic(player, player), damage);
        }
    }
}
