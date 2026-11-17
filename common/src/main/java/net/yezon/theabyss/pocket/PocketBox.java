package net.yezon.theabyss.pocket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.player.PlayerEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerInteractEvent;
import net.yezon.theabyss.platform.event.level.BlockEvent;
import net.yezon.theabyss.platform.event.tick.ServerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.util.FallGuard;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class PocketBox {
    public static final int SERVER_VIEW = 3;
    public static final int CLIENT_VIEW = 3;
    private static final int VOID_Y = PocketCells.BOTTOM - 16;
    private static final Set<UUID> SHORT_SIGHT = new HashSet<>();
    private static int lastServerView = -1;

    private PocketBox() {
    }

    public static void enter(ServerPlayer player) {
        ServerLevel pocket = player.level().getServer().getLevel(PocketCells.DIMENSION);
        if (pocket == null) {
            return;
        }
        int cell = PocketCells.get(player.level().getServer()).cellOf(player.getUUID());
        moveTo(player, pocket, cell);
    }

    private static void moveTo(ServerPlayer player, ServerLevel pocket, int cell) {
        Vec3 at = ground(pocket, PocketCells.arrival(cell));
        player.stopRiding();
        FallGuard.protect(player, 60);
        net.yezon.theabyss.util.AbyssTeleport.transition(player, new DimensionTransition(pocket, at, Vec3.ZERO, PocketCells.ARRIVAL_YAW, 0.0F, DimensionTransition.DO_NOTHING));
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        sendView(player);
    }

    private static Vec3 ground(ServerLevel pocket, Vec3 at) {
        BlockPos column = BlockPos.containing(at);
        pocket.getChunk(column.getX() >> 4, column.getZ() >> 4);
        int y = pocket.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
        if (y <= PocketCells.BOTTOM) {
            pocket.setBlockAndUpdate(new BlockPos(column.getX(), PocketCells.FLOOR, column.getZ()), Blocks.DIRT.defaultBlockState());
            y = PocketCells.FLOOR + 1;
        }
        return new Vec3(at.x, y, at.z);
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendView(player);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendView(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendView(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SHORT_SIGHT.remove(event.getEntity().getUUID());
    }

    private static void sendView(ServerPlayer player) {
        if (player.level().dimension() != PocketCells.DIMENSION) {
            if (SHORT_SIGHT.remove(player.getUUID())) {
                player.connection.send(new ClientboundSetChunkCacheRadiusPacket(player.level().getServer().getPlayerList().getViewDistance()));
            }
            return;
        }
        player.connection.send(new ClientboundSetChunkCacheRadiusPacket(CLIENT_VIEW));
        SHORT_SIGHT.add(player.getUUID());
    }

    private static boolean outside(Level level, BlockPos pos) {
        if (level.dimension() != PocketCells.DIMENSION) {
            return false;
        }
        int cell = PocketCells.at(pos.getX() + 0.5, pos.getZ() + 0.5);
        return cell < 0 || !PocketCells.inBox(cell, pos.getX() + 0.5, pos.getZ() + 0.5, -0.5) || pos.getY() > PocketCells.TOP_BLOCK;
    }

    @SubscribeEvent
    public static void onBreak(net.yezon.theabyss.platform.event.level.block.BreakBlockEvent event) {
        if (event.getLevel() instanceof Level level && outside(level, event.getPos()) && !event.getPlayer().isCreative()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof Level level && outside(level, event.getPos()) && !(event.getEntity() instanceof Player p && p.isCreative())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onUseBlock(PlayerInteractEvent.RightClickBlock event) {
        if (outside(event.getLevel(), event.getPos()) && !event.getEntity().isCreative()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onHitBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (outside(event.getLevel(), event.getPos()) && !event.getEntity().isCreative()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        ServerLevel pocket = server.getLevel(PocketCells.DIMENSION);
        if (pocket == null) {
            return;
        }
        pocket.getChunkSource().setViewDistance(SERVER_VIEW);
        int serverView = server.getPlayerList().getViewDistance();
        boolean resend = serverView != lastServerView;
        lastServerView = serverView;
        long time = pocket.getGameTime();
        for (ServerPlayer player : pocket.players()) {
            if (resend) {
                sendView(player);
            }
            if (time % 2L == 0L) {
                guard(player, pocket);
            }
        }
        if (time % 20L == 0L) {
            List<Entity> loose = new ArrayList<>();
            for (Entity entity : pocket.getAllEntities()) {
                if (entity instanceof LivingEntity && !(entity instanceof Player) || entity instanceof ItemEntity) {
                    loose.add(entity);
                }
            }
            for (Entity entity : loose) {
                keepIn(entity, pocket);
            }
        }
    }

    private static void guard(ServerPlayer player, ServerLevel pocket) {
        if (player.isSpectator()) {
            return;
        }
        int cell = PocketCells.at(player.getX(), player.getZ());
        boolean fell = player.getY() < VOID_Y;
        boolean out = cell < 0 || !PocketCells.inBox(cell, player.getX(), player.getZ(), 1.0)
            || player.getY() + player.getBbHeight() > PocketCells.CEILING + 1.0;
        if (fell || out) {
            int own = PocketCells.get(pocket.getServer()).find(player.getUUID());
            int target = own >= 0 ? own : cell >= 0 ? cell : PocketCells.get(pocket.getServer()).cellOf(player.getUUID());
            TheabyssMod.LOGGER.info("Pocket box: {} {} at {}, back to cell {}", player.getName().getString(), fell ? "fell into the void" : "left the box",
                player.position(), target);
            moveTo(player, pocket, target);
            player.displayClientMessage(Component.translatable("ring.theabyss.pocket.caught"), true);
            RingVisuals.pocketPortal(pocket, player.position());
        }
    }

    private static void keepIn(Entity entity, ServerLevel pocket) {
        int cell = PocketCells.at(entity.getX(), entity.getZ());
        if (cell < 0) {
            return;
        }
        if (entity.getY() < VOID_Y) {
            Vec3 at = ground(pocket, new Vec3(PocketCells.centreX(cell), 0.0, PocketCells.centreZ(cell)));
            net.yezon.theabyss.util.AbyssTeleport.to(entity, at.x, at.y, at.z);
            entity.setDeltaMovement(Vec3.ZERO);
            if (entity instanceof LivingEntity living) {
                FallGuard.protect(living, 60);
            }
            return;
        }
        if (PocketCells.inBox(cell, entity.getX(), entity.getZ(), 0.0)) {
            return;
        }
        double half = entity.getBbWidth() / 2.0 + 0.05;
        double x = Math.clamp(entity.getX(), PocketCells.boxX(cell) + half, PocketCells.boxX(cell) + PocketCells.BOX - half);
        double z = Math.clamp(entity.getZ(), PocketCells.boxZ(cell) + half, PocketCells.boxZ(cell) + PocketCells.BOX - half);
        Vec3 at = ground(pocket, new Vec3(x, 0.0, z));
        net.yezon.theabyss.util.AbyssTeleport.to(entity, x, Math.max(at.y, entity.getY()), z);
        entity.setDeltaMovement(Vec3.ZERO);
    }
}
