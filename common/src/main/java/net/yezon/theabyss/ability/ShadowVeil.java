package net.yezon.theabyss.ability;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.EntityJoinLevelEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingChangeTargetEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingIncomingDamageEvent;
import net.yezon.theabyss.platform.event.entity.player.AttackEntityEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerEvent;
import net.yezon.theabyss.platform.event.tick.PlayerTickEvent;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.configuration.ConfigConfiguration;
import net.yezon.theabyss.network.ShadowVeilMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.TrueInvisibility;
import net.yezon.theabyss.ring.fx.GlowParticleOptions;
import net.yezon.theabyss.ring.fx.RingFx;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class ShadowVeil {
    public static final float RADIUS = 6.0F;
    public static final int MAX = 200;
    public static final int MIN_START = 40;
    private static final double RECHARGE = 0.5;
    private static final double SOMNIUM_EPSILON = 0.01;
    private static final int COLOR = 0x8FE8FF;
    private static final ResourceKey<Level> ABYSS = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "the_abyss"));
    private static final Map<UUID, State> STATES = new HashMap<>();

    private static final class State {
        double energy = MAX;
        boolean active;
        boolean waitRelease;
        double somnium;
    }

    private ShadowVeil() {
    }

    public static boolean veiled(ServerPlayer player) {
        State state = STATES.get(player.getUUID());
        return state != null && state.active;
    }

    private static boolean intruder(ServerPlayer player) {
        return !player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(RADIUS),
            m -> m instanceof Enemy && m.isAlive() && m.distanceTo(player) <= RADIUS).isEmpty();
    }

    public static void reveal(ServerPlayer player) {
        State state = STATES.get(player.getUUID());
        if (state != null && state.active) {
            end(player, state, true);
        }
    }

    private static void start(ServerPlayer player, State state) {
        state.active = true;
        state.somnium = TheabyssModVariables.getPlayerVariables(player).Mana;
        PacketDistributor.sendToPlayer(player, new ShadowVeilMessage(true, (int)state.energy));
        TrueInvisibility.loseHunters(player, RADIUS);
    }

    private static void end(ServerPlayer player, State state, boolean broken) {
        state.active = false;
        state.waitRelease = true;
        PacketDistributor.sendToPlayer(player, new ShadowVeilMessage(false, (int)state.energy));
        if (broken) {
            ServerLevel level = player.serverLevel();
            Vec3 feet = player.position();
            RingFx.ring(level, GlowParticleOptions.of(COLOR, 0.06F, 16), feet.add(0.0, 0.3, 0.0), RADIUS, 48, 0.12, 0.03);
            RingFx.burst(level, GlowParticleOptions.of(0xE8FFFE, 0.05F, 12), feet.add(0.0, 1.0, 0.0), 12, 0.4, 0.05);
            level.playSound(null, feet.x, feet.y, feet.z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.5F, 0.6F);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        State state = STATES.computeIfAbsent(player.getUUID(), id -> new State());
        boolean sneaking = player.isShiftKeyDown();
        boolean allowed = ConfigConfiguration.SHADOW_VEIL.get() && player.level().dimension() == ABYSS && player.isAlive() && !player.isSpectator();
        if (!sneaking) {
            state.waitRelease = false;
        }
        if (state.active) {
            state.energy = Math.max(0.0, state.energy - 1.0);
            double somnium = TheabyssModVariables.getPlayerVariables(player).Mana;
            boolean cast = somnium < state.somnium - SOMNIUM_EPSILON;
            state.somnium = somnium;
            if (!allowed || !sneaking || state.energy <= 0.0) {
                end(player, state, false);
            } else if (cast || intruder(player)) {
                end(player, state, true);
            } else if (player.tickCount % 5 == 0) {
                TrueInvisibility.loseHunters(player, RADIUS);
            }
            return;
        }
        state.energy = Math.min(MAX, state.energy + RECHARGE);
        if (allowed && sneaking && !state.waitRelease && state.energy >= MIN_START && !intruder(player)) {
            start(player, state);
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() instanceof ServerPlayer player && veiled(player) && event.getEntity().distanceTo(player) > RADIUS) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            reveal(player);
        }
    }

    @SubscribeEvent
    public static void onDealDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player && event.getEntity() != player) {
            reveal(player);
        }
    }

    @SubscribeEvent
    public static void onProjectile(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && !event.loadedFromDisk() && event.getEntity() instanceof Projectile projectile
            && !(projectile instanceof FishingHook) && projectile.getOwner() instanceof ServerPlayer player) {
            reveal(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        STATES.remove(event.getEntity().getUUID());
    }
}
