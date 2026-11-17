package net.yezon.theabyss.ring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.tick.ServerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.boss.nightblade.NightbladeParry;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.network.NightbladeImpactMessage;
import net.yezon.theabyss.network.NightbladeShakeMessage;
import net.yezon.theabyss.network.TelekinesisMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.fx.RingFx;
import net.yezon.theabyss.util.FallGuard;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class ComeHere {
    public static final double RANGE = 25.0;
    public static final int LEVEL = 2;
    public static final int PARRY_LEVEL = 4;
    public static final double COST = 20.0;
    public static final int COOLDOWN = 400;
    public static final int PULL = 14;
    public static final int WINDOW = 9;
    public static final int GRACE = 3;
    private static final double STOP = 2.2;

    private static final Map<UUID, Long> READY = new HashMap<>();
    private static final List<Pull> PULLS = new ArrayList<>();

    private static final class Pull {
        final ServerLevel level;
        final ServerPlayer caster;
        final LivingEntity target;
        final boolean parry;
        final float damage;
        int age;
        Vec3 start;
        boolean struck;

        Pull(ServerLevel level, ServerPlayer caster, LivingEntity target, boolean parry, float damage) {
            this.level = level;
            this.caster = caster;
            this.target = target;
            this.parry = parry;
            this.damage = damage;
        }
    }

    private ComeHere() {
    }

    public static int latch() {
        return Telekinesis.latch(0);
    }

    public static int arrival() {
        return latch() + PULL;
    }

    public static int release() {
        return arrival() + GRACE;
    }

    public static void resetCooldown(Player player) {
        READY.remove(player.getUUID());
    }

    public static boolean pulling(Player player) {
        for (Pull pull : PULLS) {
            if (pull.caster == player && pull.parry && !pull.struck) {
                return true;
            }
        }
        return false;
    }

    public static void cast(ServerPlayer player, ItemStack ring) {
        if (ring.isEmpty() || player.isSpectator()) {
            return;
        }
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
        int damageLevel = SomniumUpgrades.level(vars, SomniumUpgrades.Kind.DAMAGE);
        if (damageLevel < LEVEL) {
            player.displayClientMessage(Component.translatable("ring.theabyss.telekinetic.come_here_locked", LEVEL), true);
            return;
        }
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        Long ready = READY.get(player.getUUID());
        if (ready != null && now < ready) {
            player.displayClientMessage(Component.translatable("ring.theabyss.telekinetic.come_here_cooldown", (int)Math.ceil((ready - now) / 20.0)), true);
            return;
        }
        LivingEntity target = net.yezon.theabyss.ring.secondary.Aim.creature(level, player, RANGE);
        if (target == null) {
            player.displayClientMessage(Component.translatable("ring.theabyss.telekinetic.no_target"), true);
            return;
        }
        RingCast cast = RingCast.start(level, player, ring, COST, player.getEyePosition());
        if (cast == null) {
            return;
        }
        READY.put(player.getUUID(), now + (long)(COOLDOWN * vars.ManaCooldown));
        boolean parry = damageLevel >= PARRY_LEVEL;
        net.yezon.theabyss.ring.fx.RingAccent.send(level, net.yezon.theabyss.ring.fx.RingAccent.TELEKINETIC, player.position(), player, java.util.List.of(target));
        net.yezon.theabyss.ring.fx.SpellCircle.on(level, net.yezon.theabyss.ring.fx.SpellCircle.RUNES, player, net.yezon.theabyss.ring.RingVisuals.TELEKINETIC, 1.4F, 20);
        net.yezon.theabyss.ring.fx.SpellCircle.on(level, net.yezon.theabyss.ring.fx.SpellCircle.RIPPLE, target, net.yezon.theabyss.ring.RingVisuals.TELEKINETIC, 1.4F, 16);
        PULLS.add(new Pull(level, player, target, parry, (float)RingDamage.PARRY.at(vars.ManaDamage)));
        TelekinesisMessage.around(level, player.position(),
            new TelekinesisMessage(parry ? TelekinesisMessage.PULL_PARRY : TelekinesisMessage.PULL, player.getId(), List.of(target.getId())));
        player.swing(hand(player, ring), true);
        sound(level, player.position(), SoundEvents.CHAIN_PLACE, 1.4F, 0.6F);
        sound(level, player.position(), SoundEvents.TRIDENT_THROW.value(), 1.0F, 0.7F);
        cast.durability(TheabyssModItems.RING_OF_TELEKINETIC.get(), 1).sound("theabyss:spell_of_telekinetic").charge(COST);
    }

    private static InteractionHand hand(Player player, ItemStack ring) {
        if (player.getOffhandItem() == ring || Gauntlet.is(player.getOffhandItem()) && !Gauntlet.is(player.getMainHandItem())
            && !player.getMainHandItem().is(TheabyssModItems.RING_OF_TELEKINETIC.get())) {
            return InteractionHand.OFF_HAND;
        }
        return InteractionHand.MAIN_HAND;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        for (Iterator<Pull> it = PULLS.iterator(); it.hasNext(); ) {
            Pull pull = it.next();
            pull.age++;
            if (!tick(pull)) {
                it.remove();
                TelekinesisMessage.around(pull.level, pull.target.position(),
                    new TelekinesisMessage(TelekinesisMessage.IMPACT, pull.caster.getId(), List.of(pull.target.getId())));
            }
        }
    }

    private static boolean tick(Pull pull) {
        LivingEntity t = pull.target;
        ServerPlayer caster = pull.caster;
        if (!t.isAlive() || t.level() != pull.level || caster.isRemoved() || caster.level() != pull.level) {
            return false;
        }
        int age = pull.age;
        if (age < latch()) {
            return true;
        }
        if (age == latch()) {
            pull.start = t.position();
            sound(pull.level, t.position(), SoundEvents.CHAIN_HIT, 1.6F, 0.55F);
            sound(pull.level, t.position(), SoundEvents.ANVIL_LAND, 0.4F, 0.7F);
            sound(pull.level, caster.position(), SoundEvents.CHAIN_BREAK, 1.0F, 0.6F);
            FallGuard.protect(t, PULL + GRACE + 60);
            caster.swing(RingMagic.swingHand(caster), true);
        }
        if (pull.parry && !pull.struck && age >= arrival() - WINDOW
            && NightbladeParry.consume(caster, age - (arrival() - WINDOW))) {
            strike(pull);
            return false;
        }
        if (age >= release()) {
            return false;
        }
        Vec3 look = caster.getLookAngle().multiply(1.0, 0.0, 1.0);
        look = look.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : look.normalize();
        Vec3 dest = caster.position().add(look.scale(STOP + t.getBbWidth() * 0.5));
        double k = Math.min(1.0, (age - latch()) / (double)PULL);
        double ease = k * k * (3.0 - 2.0 * k);
        Vec3 want = pull.start.add(dest.subtract(pull.start).scale(ease)).add(0.0, Math.sin(k * Math.PI) * 1.3, 0.0);
        Vec3 pos = t.position();
        t.setDeltaMovement(want.subtract(pos).scale(age < arrival() ? 0.9 : 0.5));
        t.resetFallDistance();
        t.hasImpulse = true;
        t.hurtMarked = true;
        if (age < arrival() && (age - latch()) % 4 == 0) {
            sound(pull.level, t.position(), SoundEvents.CHAIN_STEP, 1.0F, 0.6F + (float)k * 0.5F);
        }
        if (age == arrival()) {
            sound(pull.level, t.position(), SoundEvents.CHAIN_FALL, 1.2F, 0.6F);
        }
        return true;
    }

    private static void strike(Pull pull) {
        pull.struck = true;
        ServerLevel level = pull.level;
        ServerPlayer caster = pull.caster;
        LivingEntity t = pull.target;
        Vec3 at = t.getBoundingBox().getCenter();
        caster.swing(RingMagic.swingHand(caster), true);
        t.invulnerableTime = 0;
        t.hurt(level.damageSources().indirectMagic(caster, caster), pull.damage);
        Vec3 away = t.position().subtract(caster.position()).multiply(1.0, 0.0, 1.0);
        away = away.lengthSqr() < 1.0E-4 ? caster.getLookAngle().multiply(1.0, 0.0, 1.0) : away;
        away = away.normalize();
        t.setDeltaMovement(away.x * 1.6, 0.55, away.z * 1.6);
        t.hasImpulse = true;
        t.hurtMarked = true;
        NightbladeImpactMessage.send(caster, true);
        NightbladeShakeMessage.around(level, at, 16.0, 0.9F, 0.4F);
        RingFx.wave(level, at, 4.5F, 16, Telekinesis.COLOUR);
        sound(level, at, SoundEvents.PLAYER_ATTACK_CRIT, 1.6F, 0.7F);
        sound(level, at, SoundEvents.MACE_SMASH_GROUND_HEAVY, 1.5F, 0.9F);
        sound(level, at, SoundEvents.ANVIL_LAND, 0.7F, 1.6F);
        sound(level, at, SoundEvents.CHAIN_BREAK, 1.4F, 0.5F);
    }

    private static void sound(ServerLevel level, Vec3 at, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, at.x, at.y, at.z, sound, SoundSource.PLAYERS, volume, Mth.clamp(pitch, 0.5F, 2.0F));
    }
}
