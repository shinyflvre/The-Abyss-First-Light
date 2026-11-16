package net.yezon.theabyss.ring.echo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.living.LivingIncomingDamageEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerEvent;
import net.yezon.theabyss.platform.event.server.ServerStoppingEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.astral.AstralData;
import net.yezon.theabyss.entity.SilentEchoEntity;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.possession.PossessionManager;
import net.yezon.theabyss.ring.RingCast;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.fx.RingFx;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class SilentEchoes {
    public static final double COST = 60.0;
    public static final int COOLDOWN = 1200;
    public static final int LILAC = 0xB47CFF;
    public static final int PEARL = 0xE8E2FB;
    private static final Map<UUID, UUID> ECHOES = new HashMap<>();

    private SilentEchoes() {
    }

    public static void cast(Level world, Player caster, ItemStack ring) {
        if (!(caster instanceof ServerPlayer player) || !(world instanceof ServerLevel level)) {
            return;
        }
        if (EchoVision.active(player)) {
            EchoVision.leave(player, EchoVision.Reason.DISMISSED, null, 0.0F);
            return;
        }
        SilentEchoEntity existing = echoOf(player);
        if (existing != null) {
            dissolve(existing, existing.position());
            player.sendOverlayMessage(Component.translatable("ring.theabyss.echo.dismissed"));
            return;
        }
        if (player.isSpectator() || PossessionManager.isPossessing(player) || AstralData.isAstral(player)) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.echo.cannot"));
            return;
        }
        RingCast cast = RingCast.start(level, player, ring, COST, player.position());
        if (cast == null) {
            return;
        }
        SilentEchoEntity echo = TheabyssModEntities.SILENT_ECHO.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (echo == null) {
            return;
        }
        Vec3 at = spot(level, player, echo);
        echo.snapTo(at.x, at.y, at.z, player.getYRot(), 0.0F);
        echo.setYHeadRot(player.getYHeadRot());
        echo.setYBodyRot(player.getYRot());
        echo.bind(player);
        if (!level.addFreshEntity(echo)) {
            return;
        }
        ECHOES.put(player.getUUID(), echo.getUUID());
        SpellFxMessage.around(level, at, SpellFxMessage.ECHO_SUMMON, List.of(player.getId(), echo.getId()),
            List.of(player.getX(), player.getY(), player.getZ(), at.x, at.y, at.z, (double)player.getYRot()), 22);
        RingFx.implode(level, at.add(0.0, 0.1, 0.0), 3.5F, 16, LILAC);
        RingVisuals.sound(level, at, SoundEvents.ILLUSIONER_PREPARE_MIRROR, 0.9F, 0.85F);
        RingVisuals.sound(level, at, SoundEvents.ILLUSIONER_MIRROR_MOVE, 0.8F, 0.7F);
        player.sendOverlayMessage(Component.translatable("ring.theabyss.echo.called"));
        cast.durability(ring.getItem(), 1).charge(COST);
    }

    public static @Nullable SilentEchoEntity echoOf(ServerPlayer player) {
        UUID id = ECHOES.get(player.getUUID());
        if (id == null) {
            return null;
        }
        Entity entity = player.level().getEntity(id);
        if (entity instanceof SilentEchoEntity echo && echo.isAlive()) {
            return echo;
        }
        for (ServerLevel level : player.level().getServer().getAllLevels()) {
            if (level.getEntity(id) instanceof SilentEchoEntity echo && echo.isAlive()) {
                return echo;
            }
        }
        ECHOES.remove(player.getUUID());
        return null;
    }

    public static void dissolve(SilentEchoEntity echo, Vec3 at) {
        if (echo.isRemoved() || !(echo.level() instanceof ServerLevel level)) {
            return;
        }
        ServerPlayer player = echo.owner();
        SpellFxMessage.around(level, at, SpellFxMessage.ECHO_DISSOLVE, List.of(player != null ? player.getId() : -1),
            List.of(at.x, at.y, at.z, (double)echo.getYRot()), 30);
        RingFx.wave(level, at.add(0.0, 0.1, 0.0), 5.0F, 18, LILAC);
        RingVisuals.sound(level, at, SoundEvents.SOUL_ESCAPE.value(), 1.0F, 0.8F);
        RingVisuals.sound(level, at, SoundEvents.ILLUSIONER_MIRROR_MOVE, 0.5F, 1.4F);
        echo.discard();
    }

    public static void gone(SilentEchoEntity echo) {
        UUID owner = echo.ownerId();
        if (owner == null || !echo.getUUID().equals(ECHOES.get(owner))) {
            return;
        }
        ECHOES.remove(owner);
        ServerPlayer player = echo.owner();
        if (player == null) {
            return;
        }
        double factor = TheabyssModVariables.getPlayerVariables(player).ManaCooldown;
        player.getCooldowns().addCooldown(new ItemStack(TheabyssModItems.RING_OF_SILENT_ECHOES.get()), (int)(COOLDOWN * factor));
        EchoVision.vesselGone(player, echo);
    }

    public static void blink(SilentEchoEntity echo, Vec3 to) {
        if (!(echo.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 from = echo.position();
        echo.teleportTo(to.x, to.y, to.z);
        SpellFxMessage.around(level, from, to, new SpellFxMessage(SpellFxMessage.ECHO_LINK, List.of(),
            List.of(from.x, from.y + 1.0, from.z, to.x, to.y + 1.0, to.z), 10));
        RingVisuals.sound(level, to, SoundEvents.ILLUSIONER_MIRROR_MOVE, 0.4F, 1.3F);
    }

    private static Vec3 spot(ServerLevel level, ServerPlayer player, SilentEchoEntity echo) {
        float yaw = player.getYRot();
        float[] turns = {90.0F, -90.0F, 135.0F, -135.0F, 180.0F, 45.0F, -45.0F};
        for (float turn : turns) {
            Vec3 dir = Vec3.directionFromRotation(0.0F, yaw + turn);
            Vec3 at = player.position().add(dir.scale(1.4));
            for (int dy = 0; dy >= -2; dy--) {
                Vec3 feet = new Vec3(at.x, Math.floor(player.getY()) + dy, at.z);
                BlockPos below = BlockPos.containing(feet.x, feet.y - 0.5, feet.z);
                AABB box = echo.getDimensions(echo.getPose()).makeBoundingBox(feet);
                if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP) && level.noCollision(echo, box)) {
                    return feet;
                }
            }
        }
        return player.position();
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof SilentEchoEntity echo)) {
            return;
        }
        if (!echo.allowedTarget(event.getEntity()) && !(event.getEntity() instanceof SilentEchoEntity)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SilentEchoEntity echo = echoOf(player);
            if (echo != null) {
                dissolve(echo, echo.position());
            }
            ECHOES.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onStopping(ServerStoppingEvent event) {
        ECHOES.clear();
    }
}
