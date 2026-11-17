package net.yezon.theabyss.enchant;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.EntityJoinLevelEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingIncomingDamageEvent;
import net.yezon.theabyss.platform.event.tick.EntityTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.SomniumUpgrades;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.fx.RingFx;
import net.yezon.theabyss.util.RingMode;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class BowEnchantments {
    static final String SEEKER = "theabyss_seeker";
    static final String DRAW = "theabyss_somnium_draw";
    static final String LIGHT = "theabyss_piercing_light";
    static final String LOCK = "theabyss_seek_lock";
    private static final int LIGHT_GLOW = 0xD8FFFA;
    private static final int SEEK_GLOW = 0xB896FF;
    private static final double SEEK_RANGE = 16.0;
    private static final double SEEK_COS = Math.cos(Math.toRadians(35.0));
    private static final double SEEK_TURN = 0.18;
    private static final int SEEK_TICKS = 80;
    private static final int DRAW_GLOW = 0x30D8C8;

    private BowEnchantments() {
    }

    public static double drawCost(int power) {
        return power >= 2 ? 32.0 : 24.0;
    }

    public static float drawMultiplier(int power) {
        return power >= 2 ? 1.5F : 1.25F;
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() || !(event.getEntity() instanceof AbstractArrow arrow) || !(event.getLevel() instanceof ServerLevel level)
            || !(arrow.getOwner() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack bow = arrow.getWeaponItem();
        if (bow == null || bow.isEmpty()) {
            return;
        }
        CompoundTag data = net.yezon.theabyss.platform.PersistentData.get(arrow);
        boolean seeker = AbyssEnchantments.level(level, AbyssEnchantments.SEEKER_SHOT, bow) > 0;
        boolean light = AbyssEnchantments.level(level, AbyssEnchantments.PIERCING_LIGHT, bow) > 0;
        if (seeker) {
            data.putBoolean(SEEKER, true);
        }
        if (light) {
            data.putBoolean(LIGHT, true);
        }
        int power = AbyssEnchantments.level(level, AbyssEnchantments.SOMNIUM_DRAW, bow);
        boolean drawn = power > 0 && arrow.isCritArrow() && pay(player, power);
        int trail = drawn ? DRAW_GLOW : light ? LIGHT_GLOW : seeker ? SEEK_GLOW : 0;
        if (trail != 0) {
            SpellFxMessage.around(level, arrow.position(), SpellFxMessage.TRAIL, List.of(arrow.getId()), List.of((double)trail, (double)0xFFFFFF), 60);
        }
        if (!drawn) {
            return;
        }
        data.putFloat(DRAW, drawMultiplier(power));
        Vec3 at = arrow.position();
        SpellFxMessage.around(level, at, SpellFxMessage.ENCHANT_SHOT, List.of(), SpellFxMessage.points(at, arrow.getDeltaMovement()), 0);
        RingFx.wave(level, at, 2.5F, 10, DRAW_GLOW);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.8F, 1.6F);
    }

    private static boolean pay(ServerPlayer player, int power) {
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
        double cost = drawCost(power) * SomniumUpgrades.costFactor(vars);
        if (vars.Mana < cost) {
            return false;
        }
        double left = vars.Mana - cost;
        TheabyssModVariables.updatePlayerVariables(player, capability -> {
            capability.Mana = left;
            capability.syncPlayerVariables(player);
        });
        return true;
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof AbstractArrow arrow) || !(arrow.level() instanceof ServerLevel level)) {
            return;
        }
        CompoundTag data = net.yezon.theabyss.platform.PersistentData.get(arrow);
        boolean seeker = net.yezon.theabyss.compat.NbtCompat.getBooleanOr(data, SEEKER, false);
        float draw = net.yezon.theabyss.compat.NbtCompat.getFloatOr(data, DRAW, 0.0F);
        if (!seeker && draw <= 0.0F) {
            return;
        }
        Vec3 velocity = arrow.getDeltaMovement();
        if (velocity.lengthSqr() < 0.05) {
            return;
        }
        if (seeker && arrow.tickCount <= SEEK_TICKS) {
            steer(level, arrow, velocity);
        }
    }

    private static void steer(ServerLevel level, AbstractArrow arrow, Vec3 velocity) {
        Vec3 dir = velocity.normalize();
        Vec3 from = arrow.position();
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(from, from).inflate(SEEK_RANGE))) {
            if (!(e instanceof Enemy) || !e.isAlive() || e.isSpectator() || e == arrow.getOwner()
                || (arrow.getOwner() instanceof Player owner && RingMode.spares(owner, e))) {
                continue;
            }
            Vec3 to = e.getBoundingBox().getCenter().subtract(from);
            double dist = to.length();
            if (dist < 0.5 || dist > SEEK_RANGE) {
                continue;
            }
            double cos = to.scale(1.0 / dist).dot(dir);
            if (cos < SEEK_COS) {
                continue;
            }
            double score = dist * (2.0 - cos);
            if (score < bestScore) {
                bestScore = score;
                best = e;
            }
        }
        if (best == null) {
            return;
        }
        if (net.yezon.theabyss.compat.NbtCompat.getIntOr(net.yezon.theabyss.platform.PersistentData.get(arrow), LOCK, -1) != best.getId()) {
            net.yezon.theabyss.platform.PersistentData.get(arrow).putInt(LOCK, best.getId());
            Vec3 c = best.getBoundingBox().getCenter();
            SpellFxMessage.around(level, c, SpellFxMessage.ENCHANT_LOCK, List.of(best.getId()), SpellFxMessage.points(c), 0);
            level.playSound(null, c.x, c.y, c.z, SoundEvents.SCULK_CLICKING, SoundSource.PLAYERS, 0.7F, 1.4F);
        }
        Vec3 want = best.getBoundingBox().getCenter().subtract(from).normalize();
        Vec3 turned = dir.scale(1.0 - SEEK_TURN).add(want.scale(SEEK_TURN)).normalize().scale(velocity.length());
        arrow.setDeltaMovement(turned);
        arrow.hasImpulse = true;
    }

    @SubscribeEvent
    public static void onIncoming(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof AbstractArrow arrow) || !(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        CompoundTag data = net.yezon.theabyss.platform.PersistentData.get(arrow);
        float draw = net.yezon.theabyss.compat.NbtCompat.getFloatOr(data, DRAW, 0.0F);
        Vec3 at = event.getEntity().position().add(0.0, event.getEntity().getBbHeight() * 0.5, 0.0);
        Vec3 dir = arrow.getDeltaMovement();
        if (net.yezon.theabyss.compat.NbtCompat.getBooleanOr(data, LIGHT, false)) {
            SpellFxMessage.around(level, at, SpellFxMessage.ENCHANT_BEAM, List.of(), SpellFxMessage.points(at, dir), 0);
        }
        if (draw <= 0.0F) {
            return;
        }
        event.setAmount(event.getAmount() * draw);
        SpellFxMessage.around(level, at, SpellFxMessage.ENCHANT_HIT, List.of(), SpellFxMessage.points(at, dir), 0);
        RingFx.wave(level, at, 3.5F, 12, DRAW_GLOW);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0F, 0.6F);
    }
}
