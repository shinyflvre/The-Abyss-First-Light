package net.yezon.theabyss.events;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModSounds;
import net.yezon.theabyss.network.ClockRewindMessage;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.fx.RingFx;
import net.yezon.theabyss.util.FallGuard;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.SomniumUpgrades;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class ApplyClockOfTimeEvents {
    private static final double COST = 50.0;
    private static final String DIMENSION_KEY = "theabyss_clock_dimension";
    private static final int TRACK_TICKS = 100;
    private static final int SAMPLE_EVERY = 2;
    private static final int SAMPLES = TRACK_TICKS / SAMPLE_EVERY;
    private static final int REWIND_TICKS = 22;
    private static final int SAVE_LOCK = 20;
    private static final int ARRIVAL_FALL_GUARD = 40;
    private static final int RECALL_LOCK = REWIND_TICKS + 10;
    private static final int FX_TICKS = 30;
    private static final int GOLD = 0xFFD36A;
    private static final int CYAN = 0x7FE8FF;
    private static final Map<UUID, Track> TRACKS = new ConcurrentHashMap<>();
    private static final Set<UUID> REWINDING = ConcurrentHashMap.newKeySet();

    private ApplyClockOfTimeEvents() {
    }

    private static final class Track {
        final ResourceKey<Level> startDimension;
        final List<ClockRewindMessage.Sample> first = new ArrayList<>();
        final ArrayDeque<ClockRewindMessage.Sample> recent = new ArrayDeque<>();
        ResourceKey<Level> recentDimension;
        boolean firstOpen = true;

        Track(ResourceKey<Level> dimension, ClockRewindMessage.Sample checkpoint) {
            this.startDimension = dimension;
            this.recentDimension = dimension;
            this.first.add(checkpoint);
        }
    }

    public static void execute(LevelAccessor world, Entity entity, ItemStack itemstack) {
        if (!(entity instanceof ServerPlayer player) || !(world instanceof ServerLevel level) || REWINDING.contains(player.getUUID())) {
            return;
        }
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
        if (vars.Mana < COST * SomniumUpgrades.costFactor(vars)) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.low_energy"));
            return;
        }
        if (itemstack.is(TheabyssModItems.CLOCK_OF_TIME.get())) {
            itemstack.hurtAndBreak(1, level, null, broken -> {
            });
        }
        if (!vars.ClockActive) {
            vars.ClockX = Math.round(player.getX());
            vars.ClockY = Math.round(player.getY());
            vars.ClockZ = Math.round(player.getZ());
            vars.SaveHP = Math.round(player.getHealth());
            vars.SaveFD = Math.round((float)player.getFoodData().getFoodLevel());
            vars.ClockActive = true;
            vars.syncPlayerVariables(player);
            player.getPersistentData().putString(DIMENSION_KEY, player.level().dimension().identifier().toString());
            TRACKS.put(player.getUUID(), new Track(player.level().dimension(), sample(player)));
            player.getCooldowns().addCooldown(itemstack, SAVE_LOCK);
            player.sendOverlayMessage(Component.translatable("ring.theabyss.timestamp"));
            player.sendSystemMessage(Component.translatable("ring.theabyss.timestamp"));
            return;
        }
        vars.Mana -= COST * SomniumUpgrades.costFactor(vars);
        vars.syncPlayerVariables(player);
        Track track = TRACKS.get(player.getUUID());
        boolean sameDimension = track != null && track.recentDimension == player.level().dimension();
        List<ClockRewindMessage.Sample> recent = sameDimension ? new ArrayList<>(track.recent) : new ArrayList<>();
        recent.add(sample(player));
        List<ClockRewindMessage.Sample> first = track != null ? new ArrayList<>(track.first) : new ArrayList<>();
        ClockRewindMessage.send(player, recent, first);
        Vec3 from = player.position();
        trail(level, reversed(recent), player.getEyeHeight());
        RingFx.wave(level, from.add(0.0, 0.1, 0.0), 5.0F, 16, GOLD);
        RingVisuals.sound(level, from, SoundEvents.BEACON_DEACTIVATE, 1.4F, 1.8F);
        player.getCooldowns().addCooldown(itemstack, RECALL_LOCK);
        REWINDING.add(player.getUUID());
        TheabyssMod.queueServerWork(REWIND_TICKS, () -> finish(player));
    }

    private static void finish(ServerPlayer player) {
        REWINDING.remove(player.getUUID());
        if (player.isRemoved() || !player.isAlive()) {
            return;
        }
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
        player.getFoodData().setFoodLevel((int)vars.SaveFD);
        player.setHealth((float)vars.SaveHP);
        ServerLevel destination = savedLevel(player);
        if (destination == player.level()) {
            player.teleportTo(vars.ClockX, vars.ClockY, vars.ClockZ);
        } else {
            player.teleportTo(destination, vars.ClockX, vars.ClockY, vars.ClockZ, Set.of(), player.getYRot(), player.getXRot(), true);
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        FallGuard.protect(player, ARRIVAL_FALL_GUARD);
        vars.ClockActive = false;
        vars.syncPlayerVariables(player);
        player.getPersistentData().remove(DIMENSION_KEY);
        Track track = TRACKS.remove(player.getUUID());
        ServerLevel here = player.level();
        Vec3 at = player.position();
        if (track != null) {
            trail(here, reversed(track.first), player.getEyeHeight());
        }
        RingFx.wave(here, at.add(0.0, 0.1, 0.0), 5.0F, 16, CYAN);
        RingVisuals.sound(here, at, TheabyssModSounds.SPELL_OF_TIME.get(), 1.0F, 1.4F);
        RingVisuals.sound(here, at, SoundEvents.AMETHYST_BLOCK_RESONATE, 1.6F, 0.6F);
        RingVisuals.sound(here, at, SoundEvents.ILLUSIONER_PREPARE_MIRROR, 1.0F, 1.5F);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && REWINDING.contains(player.getUUID())
            && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Track track = TRACKS.get(player.getUUID());
        if (track == null || REWINDING.contains(player.getUUID()) || player.tickCount % SAMPLE_EVERY != 0) {
            return;
        }
        if (!TheabyssModVariables.getPlayerVariables(player).ClockActive) {
            TRACKS.remove(player.getUUID());
            return;
        }
        ResourceKey<Level> dimension = player.level().dimension();
        ClockRewindMessage.Sample now = sample(player);
        if (track.firstOpen) {
            if (dimension == track.startDimension && track.first.size() < SAMPLES) {
                track.first.add(now);
            } else {
                track.firstOpen = false;
            }
        }
        if (dimension != track.recentDimension) {
            track.recent.clear();
            track.recentDimension = dimension;
        }
        track.recent.addLast(now);
        while (track.recent.size() > SAMPLES) {
            track.recent.removeFirst();
        }
    }

    private static List<ClockRewindMessage.Sample> reversed(List<ClockRewindMessage.Sample> path) {
        List<ClockRewindMessage.Sample> copy = new ArrayList<>(path);
        Collections.reverse(copy);
        return copy;
    }

    private static void trail(ServerLevel level, List<ClockRewindMessage.Sample> path, float eyeHeight) {
        if (path.size() < 2) {
            return;
        }
        Vec3[] feet = new Vec3[path.size()];
        for (int i = 0; i < feet.length; i++) {
            feet[i] = path.get(i).eye().subtract(0.0, eyeHeight, 0.0);
        }
        List<Double> data = new ArrayList<>();
        data.add((double)path.get(feet.length - 1).yaw());
        data.add((double)path.get(feet.length - 1).pitch());
        data.addAll(SpellFxMessage.points(feet));
        SpellFxMessage.around(level, feet[0], feet[feet.length - 1], new SpellFxMessage(SpellFxMessage.REWIND, List.of(), data, FX_TICKS));
    }

    private static ClockRewindMessage.Sample sample(ServerPlayer player) {
        return new ClockRewindMessage.Sample(player.getEyePosition(), player.getYRot(), player.getXRot());
    }

    private static ServerLevel savedLevel(ServerPlayer player) {
        String saved = player.getPersistentData().getStringOr(DIMENSION_KEY, "");
        Identifier id = saved.isEmpty() ? null : Identifier.tryParse(saved);
        ServerLevel level = id == null ? null : player.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, id));
        return level != null ? level : player.level();
    }
}
