package net.yezon.theabyss.ring.echo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.living.LivingDamageEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingDeathEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingIncomingDamageEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerEvent;
import net.yezon.theabyss.platform.event.server.ServerStoppingEvent;
import net.yezon.theabyss.platform.event.tick.PlayerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.astral.AstralData;
import net.yezon.theabyss.entity.SilentEchoEntity;
import net.yezon.theabyss.network.EchoVisionMessage;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.possession.PossessionManager;
import net.yezon.theabyss.ring.RingCast;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.SomniumUpgrades;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class EchoVision {
    public static final double COST = 20.0;
    public static final int COOLDOWN = 200;
    private static final String KEY = "theabyss_echo_vision";

    public enum Reason {
        VOLUNTARY("ring.theabyss.echo.returned"),
        DISMISSED("ring.theabyss.echo.dismissed"),
        TOO_FAR("ring.theabyss.echo.too_far_faded"),
        ECHO_FELL("ring.theabyss.echo.fell"),
        BODY_HIT("ring.theabyss.echo.body_hit"),
        VESSEL_LOST("ring.theabyss.echo.fell"),
        WORLD("ring.theabyss.echo.fell"),
        LOGOUT("");

        final String message;

        Reason(String message) {
            this.message = message;
        }
    }

    private record Session(UUID echo, ResourceKey<Level> dimension, Vec3 body, float yaw, double range, float realHealth, Runnable release) {
    }

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final Map<UUID, Long> READY = new HashMap<>();
    private static final Set<UUID> PENDING = new HashSet<>();

    private EchoVision() {
    }

    public static double range(int usageLevel) {
        return usageLevel >= 3 ? 128.0 : usageLevel == 2 ? 64.0 : usageLevel == 1 ? 32.0 : 16.0;
    }

    public static double range(TheabyssModVariables.PlayerVariables vars) {
        return range(SomniumUpgrades.level(vars, SomniumUpgrades.Kind.USAGE));
    }

    public static boolean active(ServerPlayer player) {
        return SESSIONS.containsKey(player.getUUID());
    }

    public static boolean isVessel(ServerPlayer player, SilentEchoEntity echo) {
        Session session = SESSIONS.get(player.getUUID());
        return session != null && session.echo().equals(echo.getUUID());
    }

    public static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        if (active(player)) {
            leave(player, Reason.VOLUNTARY, null, 0.0F);
            return true;
        }
        SilentEchoEntity echo = SilentEchoes.echoOf(player);
        if (echo == null) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.echo.no_echo"));
            return false;
        }
        if (player.isPassenger() || player.isSpectator() || player.isSleeping() || PossessionManager.isPossessing(player) || AstralData.isAstral(player)
            || echo.level() != player.level()) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.echo.cannot"));
            return false;
        }
        long now = player.level().getGameTime();
        Long ready = READY.get(player.getUUID());
        if (ready != null && now < ready) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.secondary.cooldown",
                Component.translatable("tooltip.theabyss.ring_of_silent_echoes.secondary.ability"), (int)Math.ceil((ready - now) / 20.0)));
            return false;
        }
        double range = range(vars);
        if (echo.distanceTo(player) > range) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.echo.too_far", (int)range));
            return false;
        }
        RingCast cast = RingCast.start(player.level(), player, ring, COST, player.getEyePosition());
        if (cast == null) {
            return false;
        }
        enter(player, echo, range);
        cast.durability(ring.getItem(), 1).charge(COST);
        return true;
    }

    private static void enter(ServerPlayer player, SilentEchoEntity echo, double range) {
        ServerLevel level = player.level();
        Vec3 body = player.position();
        float yaw = player.getYRot();
        float pitch = player.getXRot();
        float headYaw = player.getYHeadRot();
        Vec3 fromEye = player.getEyePosition();
        Vec3 echoAt = echo.position();
        float echoYaw = echo.getYHeadRot();
        float realHealth = player.getHealth();
        float echoHealth = Math.max(1.0F, echo.getHealth());

        SESSIONS.put(player.getUUID(), new Session(echo.getUUID(), level.dimension(), body, yaw, range, realHealth, hold(level, body)));
        CompoundTag saved = new CompoundTag();
        saved.putString("dim", level.dimension().identifier().toString());
        saved.putDouble("x", body.x);
        saved.putDouble("y", body.y);
        saved.putDouble("z", body.z);
        saved.putFloat("yaw", yaw);
        saved.putFloat("health", realHealth);
        net.yezon.theabyss.platform.PersistentData.get(player).put(KEY, saved);

        echo.becomeVessel(body, yaw, headYaw, realHealth);
        EchoVisionMessage.send(player, true, fromEye, yaw, pitch, body, (float)range);
        player.teleportTo(level, echoAt.x, echoAt.y, echoAt.z, Set.of(), echoYaw, pitch, true);
        player.setHealth(Math.min(echoHealth, player.getMaxHealth()));
        player.resetFallDistance();

        SpellFxMessage.around(level, body, echoAt, new SpellFxMessage(SpellFxMessage.ECHO_LINK, List.of(),
            List.of(body.x, body.y + 1.2, body.z, echoAt.x, echoAt.y + 1.2, echoAt.z), 16));
        RingVisuals.sound(level, body, SoundEvents.ILLUSIONER_MIRROR_MOVE, 0.7F, 0.6F);
        RingVisuals.sound(level, echoAt, SoundEvents.ILLUSIONER_PREPARE_MIRROR, 0.5F, 1.2F);
        player.sendOverlayMessage(Component.translatable("ring.theabyss.echo.vision", (int)range));
    }

    public static void leave(ServerPlayer player, Reason reason, @Nullable DamageSource hit, float hitAmount) {
        Session session = SESSIONS.remove(player.getUUID());
        PENDING.remove(player.getUUID());
        if (session == null) {
            return;
        }
        net.yezon.theabyss.platform.PersistentData.get(player).remove(KEY);
        session.release().run();
        MinecraftServer server = player.level().getServer();
        ServerLevel bodyLevel = server.getLevel(session.dimension());
        if (bodyLevel == null) {
            bodyLevel = player.level();
        }
        SilentEchoEntity vessel = bodyLevel.getEntity(session.echo()) instanceof SilentEchoEntity v && !v.isRemoved() ? v : null;
        Vec3 body = vessel != null ? vessel.position() : session.body();
        float bodyYaw = vessel != null ? vessel.getYRot() : session.yaw();
        Vec3 echoAt = player.position();
        float echoYaw = player.getYRot();
        float echoHealth = player.getHealth();
        boolean keep = reason == Reason.VOLUNTARY && vessel != null && player.isAlive() && player.level() == bodyLevel;

        EchoVisionMessage.send(player, false, player.getEyePosition(), player.getYRot(), player.getXRot(), body, (float)session.range());
        player.teleportTo(bodyLevel, body.x, body.y, body.z, Set.of(), bodyYaw, player.getXRot(), true);
        player.setHealth(Math.max(1.0F, Math.min(session.realHealth(), player.getMaxHealth())));
        player.resetFallDistance();
        player.clearFire();

        if (vessel != null) {
            if (keep) {
                vessel.leaveVessel(echoAt, echoYaw, echoHealth);
                SpellFxMessage.around(bodyLevel, body, echoAt, new SpellFxMessage(SpellFxMessage.ECHO_LINK, List.of(),
                    List.of(echoAt.x, echoAt.y + 1.2, echoAt.z, body.x, body.y + 1.2, body.z), 16));
                RingVisuals.sound(bodyLevel, body, SoundEvents.ILLUSIONER_MIRROR_MOVE, 0.7F, 0.9F);
            } else {
                SilentEchoes.dissolve(vessel, player.level() == bodyLevel ? echoAt : vessel.position());
            }
        }
        double factor = TheabyssModVariables.getPlayerVariables(player).ManaCooldown;
        READY.put(player.getUUID(), bodyLevel.getGameTime() + (long)(COOLDOWN * factor));
        if (!reason.message.isEmpty()) {
            player.sendOverlayMessage(Component.translatable(reason.message));
        }
        if (hit != null && hitAmount > 0.0F) {
            player.hurtServer(bodyLevel, hit, hitAmount);
        }
    }

    static void vesselGone(ServerPlayer player, SilentEchoEntity echo) {
        if (isVessel(player, echo)) {
            leave(player, Reason.VESSEL_LOST, null, 0.0F);
        }
    }

    private static Runnable hold(ServerLevel level, Vec3 at) {
        ChunkPos chunk = ChunkPos.containing(net.minecraft.core.BlockPos.containing(at));
        if (level.getForceLoadedChunks().contains(chunk.pack())) {
            return () -> {
            };
        }
        level.setChunkForced(chunk.x(), chunk.z(), true);
        return () -> level.setChunkForced(chunk.x(), chunk.z(), false);
    }

    private static void later(ServerPlayer player, Reason reason, @Nullable DamageSource hit, float amount) {
        if (!PENDING.add(player.getUUID())) {
            return;
        }
        UUID id = player.getUUID();
        MinecraftServer server = player.level().getServer();
        TheabyssMod.queueServerWork(1, () -> {
            ServerPlayer now = server.getPlayerList().getPlayer(id);
            if (now != null) {
                leave(now, reason, hit, amount);
            } else {
                PENDING.remove(id);
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Session session = SESSIONS.get(player.getUUID());
        if (session == null || PENDING.contains(player.getUUID())) {
            return;
        }
        if (player.level().dimension() != session.dimension()) {
            leave(player, Reason.WORLD, null, 0.0F);
            return;
        }
        Entity vessel = player.level().getEntity(session.echo());
        if (!(vessel instanceof SilentEchoEntity) || vessel.isRemoved()) {
            leave(player, Reason.VESSEL_LOST, null, 0.0F);
            return;
        }
        if (player.position().distanceTo(vessel.position()) > session.range()) {
            leave(player, Reason.TOO_FAR, null, 0.0F);
        }
    }

    @SubscribeEvent
    public static void onVesselHit(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof SilentEchoEntity echo) || !echo.isVessel()) {
            return;
        }
        ServerPlayer owner = echo.owner();
        if (owner == null || !isVessel(owner, echo)) {
            return;
        }
        event.setCanceled(true);
        if (event.getAmount() > 0.0F) {
            later(owner, Reason.BODY_HIT, event.getSource(), event.getAmount());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEchoDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !SESSIONS.containsKey(player.getUUID())) {
            return;
        }
        if (event.getSource().is(DamageTypes.GENERIC_KILL)) {
            return;
        }
        if (PENDING.contains(player.getUUID())) {
            event.setNewDamage(0.0F);
            return;
        }
        if (event.getNewDamage() >= player.getHealth() + player.getAbsorptionAmount()) {
            event.setNewDamage(0.0F);
            later(player, Reason.ECHO_FELL, null, 0.0F);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Session session = SESSIONS.remove(player.getUUID());
        PENDING.remove(player.getUUID());
        if (session == null) {
            return;
        }
        net.yezon.theabyss.platform.PersistentData.get(player).remove(KEY);
        session.release().run();
        ServerLevel bodyLevel = player.level().getServer().getLevel(session.dimension());
        if (bodyLevel != null && bodyLevel.getEntity(session.echo()) instanceof SilentEchoEntity vessel) {
            SilentEchoes.dissolve(vessel, vessel.position());
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            leave(player, Reason.LOGOUT, null, 0.0F);
            READY.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || active(player)) {
            return;
        }
        CompoundTag saved = net.yezon.theabyss.platform.PersistentData.get(player).getCompoundOrEmpty(KEY);
        if (!saved.contains("x")) {
            return;
        }
        net.yezon.theabyss.platform.PersistentData.get(player).remove(KEY);
        ServerLevel level = player.level();
        Identifier id = Identifier.tryParse(saved.getStringOr("dim", ""));
        if (id != null) {
            ServerLevel target = player.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, id));
            if (target != null) {
                level = target;
            }
        }
        player.teleportTo(level, saved.getDoubleOr("x", player.getX()), saved.getDoubleOr("y", player.getY()), saved.getDoubleOr("z", player.getZ()),
            Set.of(), saved.getFloatOr("yaw", player.getYRot()), player.getXRot(), true);
        player.setHealth(Math.max(1.0F, Math.min(saved.getFloatOr("health", player.getHealth()), player.getMaxHealth())));
        player.resetFallDistance();
    }

    @SubscribeEvent
    public static void onStopping(ServerStoppingEvent event) {
        SESSIONS.clear();
        READY.clear();
        PENDING.clear();
    }
}
