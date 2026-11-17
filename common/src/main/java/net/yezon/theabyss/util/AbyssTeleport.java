package net.yezon.theabyss.util;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.mixin.accessor.ServerGamePacketListenerFlyingAccessor;
import net.yezon.theabyss.network.TeleportMotionMessage;
import org.jspecify.annotations.Nullable;

public final class AbyssTeleport {
    private static final int KEPT_MOTION_LIMIT = 64;
    private static final Queue<Vec3> KEPT_MOTION = new ConcurrentLinkedQueue<>();

    private AbyssTeleport() {
    }

    public static void to(Entity entity, double x, double y, double z) {
        if (!(entity instanceof ServerPlayer player)) {
            entity.teleportTo(x, y, z);
            return;
        }
        Vec3 motion = player.getKnownMovement();
        float yRot = player.getYRot();
        float xRot = Mth.clamp(player.getXRot(), -90.0F, 90.0F);
        TeleportMotionMessage.send(player, x, y, z);
        player.teleportTo(x, y, z);
        settle(player, x, y, z, yRot, xRot, motion);
    }

    public static void connection(ServerPlayer player, double x, double y, double z, float yRot, float xRot) {
        player.connection.teleport(x, y, z, yRot, xRot);
        settle(player, x, y, z, yRot, Mth.clamp(xRot, -90.0F, 90.0F), Vec3.ZERO);
    }

    public static boolean level(ServerPlayer player, ServerLevel level, double x, double y, double z, float yRot, float xRot) {
        if (player.isSleeping()) {
            player.stopSleepInBed(true, true);
        }
        player.setCamera(player);
        if (transition(player, new DimensionTransition(level, new Vec3(x, y, z), Vec3.ZERO, yRot, xRot, DimensionTransition.DO_NOTHING), false) == null) {
            return false;
        }
        player.setYHeadRot(yRot);
        ServerGamePacketListenerFlyingAccessor flying = (ServerGamePacketListenerFlyingAccessor)player.connection;
        flying.theabyss$setAboveGroundTickCount(0);
        flying.theabyss$setAboveGroundVehicleTickCount(0);
        return true;
    }

    public static @Nullable Entity transition(Entity entity, DimensionTransition transition) {
        return transition(entity, transition, false);
    }

    private static @Nullable Entity transition(Entity entity, DimensionTransition transition, boolean asPassenger) {
        if (entity instanceof ServerPlayer player) {
            return player(player, transition, asPassenger);
        }
        if (!(entity.level() instanceof ServerLevel from) || entity.isRemoved()) {
            return entity.changeDimension(transition);
        }
        boolean otherDimension = transition.newLevel().dimension() != from.dimension();
        Vec3 oldPos = entity.position();
        float oldYRot = entity.getYRot();
        float oldXRot = entity.getXRot();
        float oldHeadRot = entity.getYHeadRot();
        List<Entity> riders = List.copyOf(entity.getPassengers());
        entity.ejectPassengers();
        entity.setYRot(transition.yRot());
        entity.setXRot(Mth.clamp(transition.xRot(), -90.0F, 90.0F));
        entity.setYHeadRot(transition.yRot());
        Entity moved = entity.changeDimension(transition);
        if (moved == null) {
            entity.setYRot(oldYRot);
            entity.setXRot(oldXRot);
            entity.setYHeadRot(oldHeadRot);
            for (Entity rider : riders) {
                rider.startRiding(entity, true);
            }
            return null;
        }
        if (otherDimension) {
            moved.setYBodyRot(oldYRot);
        }
        for (Entity rider : riders) {
            Vec3 offset = rider.position().subtract(oldPos);
            DimensionTransition riderTransition = new DimensionTransition(transition.newLevel(), transition.pos().add(offset), transition.speed(),
                transition.yRot() + (rider.getYRot() - oldYRot), transition.xRot() + (rider.getXRot() - oldXRot), transition.missingRespawnBlock(),
                transition.postDimensionTransition());
            Entity movedRider = transition(rider, riderTransition, true);
            if (movedRider != null) {
                movedRider.startRiding(moved, true);
            }
        }
        if (otherDimension) {
            spectators(entity, transition, from);
        }
        return moved;
    }

    private static @Nullable Entity player(ServerPlayer player, DimensionTransition transition, boolean asPassenger) {
        ServerLevel from = player.serverLevel();
        boolean otherDimension = transition.newLevel().dimension() != from.dimension();
        Entity vehicle = asPassenger || player.isRemoved() ? null : player.getVehicle();
        if (vehicle != null) {
            player.stopRiding();
            player.connection.send(new ClientboundSetPassengersPacket(vehicle));
        }
        float oldHeadRot = player.getYHeadRot();
        Vec3 oldMotion = player.getDeltaMovement();
        player.setYHeadRot(transition.yRot());
        player.setDeltaMovement(transition.speed());
        Entity moved = player.changeDimension(transition);
        if (moved == null) {
            player.setYHeadRot(oldHeadRot);
            player.setDeltaMovement(oldMotion);
            if (vehicle != null && !vehicle.isRemoved() && player.getVehicle() == null) {
                player.startRiding(vehicle, true);
            }
            return null;
        }
        Vec3 at = transition.pos();
        if (player.getX() == at.x && player.getY() == at.y && player.getZ() == at.z) {
            player.moveTo(at.x, at.y, at.z, transition.yRot(), Mth.clamp(transition.xRot(), -90.0F, 90.0F));
        }
        if (otherDimension) {
            player.stopUsingItem();
            spectators(player, transition, from);
        }
        return moved;
    }

    private static void spectators(Entity entity, DimensionTransition transition, ServerLevel from) {
        for (ServerPlayer spectator : List.copyOf(from.players())) {
            if (spectator.getCamera() == entity) {
                transition(spectator, transition, false);
                spectator.setCamera(null);
            }
        }
    }

    private static void settle(ServerPlayer player, double x, double y, double z, float yRot, float xRot, Vec3 motion) {
        player.moveTo(x, y, z, yRot, xRot);
        player.setYHeadRot(yRot);
        player.setDeltaMovement(motion);
    }

    public static void expectKeptMotion(double x, double y, double z) {
        while (KEPT_MOTION.size() >= KEPT_MOTION_LIMIT) {
            KEPT_MOTION.poll();
        }
        KEPT_MOTION.add(new Vec3(x, y, z));
    }

    public static boolean keepsMotion(ClientboundPlayerPositionPacket packet) {
        Set<RelativeMovement> relatives = packet.getRelativeArguments();
        if (relatives.contains(RelativeMovement.X) || relatives.contains(RelativeMovement.Y) || relatives.contains(RelativeMovement.Z)
            || !relatives.contains(RelativeMovement.Y_ROT) || !relatives.contains(RelativeMovement.X_ROT)) {
            return false;
        }
        Iterator<Vec3> pending = KEPT_MOTION.iterator();
        while (pending.hasNext()) {
            Vec3 at = pending.next();
            if (at.x == packet.getX() && at.y == packet.getY() && at.z == packet.getZ()) {
                pending.remove();
                return true;
            }
        }
        return false;
    }
}
