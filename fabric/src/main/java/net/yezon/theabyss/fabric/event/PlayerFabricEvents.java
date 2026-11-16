package net.yezon.theabyss.fabric.event;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.event.entity.player.AttackEntityEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerContainerEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerInteractEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerWakeUpEvent;

public final class PlayerFabricEvents {
    private PlayerFabricEvents() {
    }

    public static void loggedIn(ServerPlayer player) {
        if (AbyssEventBus.hasListeners(PlayerEvent.PlayerLoggedInEvent.class)) {
            AbyssEventBus.post(new PlayerEvent.PlayerLoggedInEvent(player));
        }
    }

    public static void loggedOut(ServerPlayer player) {
        if (AbyssEventBus.hasListeners(PlayerEvent.PlayerLoggedOutEvent.class)) {
            AbyssEventBus.post(new PlayerEvent.PlayerLoggedOutEvent(player));
        }
    }

    public static void respawn(ServerPlayer player) {
        if (AbyssEventBus.hasListeners(PlayerEvent.PlayerRespawnEvent.class)) {
            AbyssEventBus.post(new PlayerEvent.PlayerRespawnEvent(player));
        }
    }

    public static void changedDimension(ServerPlayer player, ResourceKey<Level> from, ResourceKey<Level> to) {
        if (AbyssEventBus.hasListeners(PlayerEvent.PlayerChangedDimensionEvent.class)) {
            AbyssEventBus.post(new PlayerEvent.PlayerChangedDimensionEvent(player, from, to));
        }
    }

    public static void clone(ServerPlayer player, ServerPlayer original) {
        if (AbyssEventBus.hasListeners(PlayerEvent.Clone.class)) {
            AbyssEventBus.post(new PlayerEvent.Clone(player, original));
        }
    }

    public static void startTracking(ServerPlayer player, Entity target) {
        if (AbyssEventBus.hasListeners(PlayerEvent.StartTracking.class)) {
            AbyssEventBus.post(new PlayerEvent.StartTracking(player, target));
        }
    }

    public static void containerOpen(ServerPlayer player) {
        if (AbyssEventBus.hasListeners(PlayerContainerEvent.Open.class)) {
            AbyssEventBus.post(new PlayerContainerEvent.Open(player));
        }
    }

    public static boolean attackEntity(Player player, Entity target) {
        return AbyssEventBus.hasListeners(AttackEntityEvent.class) && AbyssEventBus.post(new AttackEntityEvent(player, target)).isCanceled();
    }

    public static boolean entityInteract(Player player, InteractionHand hand, Entity target) {
        return AbyssEventBus.hasListeners(PlayerInteractEvent.EntityInteract.class)
            && AbyssEventBus.post(new PlayerInteractEvent.EntityInteract(player, player.getItemInHand(hand), target)).isCanceled();
    }

    public static boolean rightClickItem(Player player, InteractionHand hand) {
        return AbyssEventBus.hasListeners(PlayerInteractEvent.RightClickItem.class)
            && AbyssEventBus.post(new PlayerInteractEvent.RightClickItem(player, player.getItemInHand(hand))).isCanceled();
    }

    public static boolean leftClickBlock(Player player, BlockPos pos) {
        return AbyssEventBus.hasListeners(PlayerInteractEvent.LeftClickBlock.class)
            && AbyssEventBus.post(new PlayerInteractEvent.LeftClickBlock(player, player.level(), pos)).isCanceled();
    }

    public static RightClick rightClickBlock(Player player, InteractionHand hand, BlockPos pos) {
        if (!AbyssEventBus.hasListeners(PlayerInteractEvent.RightClickBlock.class)) {
            return RightClick.NONE;
        }
        PlayerInteractEvent.RightClickBlock event = AbyssEventBus.post(
            new PlayerInteractEvent.RightClickBlock(player, hand, player.getItemInHand(hand), player.level(), pos, TriState.DEFAULT));
        return event.isCanceled() ? RightClick.CANCELED : new RightClick(false, event.getUseItem());
    }

    public static void wakeUp(Player player) {
        if (AbyssEventBus.hasListeners(PlayerWakeUpEvent.class)) {
            AbyssEventBus.post(new PlayerWakeUpEvent(player));
        }
    }

    public record RightClick(boolean canceled, TriState useItem) {
        public static final RightClick NONE = new RightClick(false, TriState.DEFAULT);
        public static final RightClick CANCELED = new RightClick(true, TriState.FALSE);
    }
}
