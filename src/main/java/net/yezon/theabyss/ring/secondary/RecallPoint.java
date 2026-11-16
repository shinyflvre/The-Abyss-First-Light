package net.yezon.theabyss.ring.secondary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.RingCast;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.fx.RingFx;
import net.yezon.theabyss.util.FallGuard;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class RecallPoint {
    public static final double COST = 30.0;
    public static final int COOLDOWN = 300;
    private static final String KEY = "theabyss_recall_point";
    private static final int BEACON = 72000;
    private static final Map<UUID, Long> READY = new HashMap<>();

    private RecallPoint() {
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.level();
        CompoundTag data = player.getPersistentData();
        CompoundTag mark = data.getCompoundOrEmpty(KEY);
        boolean marked = mark.contains("x");
        if (player.isShiftKeyDown() && marked) {
            data.remove(KEY);
            SpellFxMessage.around(level, player.position(), SpellFxMessage.RECALL_MARK, List.of(player.getId()), List.of(), 0);
            player.sendOverlayMessage(Component.translatable("ring.theabyss.home.cleared"));
            RingVisuals.sound(level, player.position(), SoundEvents.BEACON_DEACTIVATE, 0.8F, 1.4F);
            return true;
        }
        if (!marked) {
            CompoundTag put = new CompoundTag();
            put.putString("dim", level.dimension().identifier().toString());
            put.putDouble("x", player.getX());
            put.putDouble("y", player.getY());
            put.putDouble("z", player.getZ());
            put.putFloat("yaw", player.getYRot());
            put.putFloat("pitch", player.getXRot());
            data.put(KEY, put);
            beacon(level, player, player.position());
            RingFx.pillar(level, player.position(), 6.0F, 20, RingVisuals.ABYSS, 0.6F);
            player.sendOverlayMessage(Component.translatable("ring.theabyss.home.marked"));
            RingVisuals.sound(level, player.position(), SoundEvents.BEACON_ACTIVATE, 1.0F, 1.4F);
            RingVisuals.sound(level, player.position(), SoundEvents.AMETHYST_BLOCK_CHIME, 1.4F, 0.8F);
            return true;
        }
        if (!mark.getStringOr("dim", "").equals(level.dimension().identifier().toString())) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.home.other_world"));
            return false;
        }
        long now = level.getGameTime();
        Long ready = READY.get(player.getUUID());
        if (ready != null && now < ready) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.secondary.cooldown", Component.translatable("tooltip.theabyss.ring_of_home.secondary.ability"), (int)Math.ceil((ready - now) / 20.0)));
            return false;
        }
        RingCast cast = RingCast.start(level, player, ring, COST, player.position());
        if (cast == null) {
            return false;
        }
        Vec3 from = player.position();
        Vec3 to = new Vec3(mark.getDoubleOr("x", from.x), mark.getDoubleOr("y", from.y), mark.getDoubleOr("z", from.z));
        float yaw = mark.getFloatOr("yaw", player.getYRot());
        float pitch = mark.getFloatOr("pitch", player.getXRot());
        SpellFxMessage.around(level, from, to, new SpellFxMessage(SpellFxMessage.RECALL, List.of(player.getId()), SpellFxMessage.points(from, to), 30));
        RingVisuals.teleportDepart(level, from, player.getBbHeight());
        if (player.isPassenger()) {
            player.stopRiding();
        }
        player.teleportTo(to.x, to.y, to.z);
        player.connection.teleport(to.x, to.y, to.z, yaw, pitch);
        player.resetFallDistance();
        FallGuard.protect(player, 60);
        data.remove(KEY);
        RingFx.pillar(level, to, 8.0F, 24, RingVisuals.ABYSS, 1.0F);
        RingFx.wave(level, to.add(0.0, 0.1, 0.0), 5.0F, 16, RingVisuals.ABYSS);
        RingVisuals.teleportArrive(level, to);
        RingVisuals.sound(level, from, SoundEvents.BEACON_POWER_SELECT, 1.2F, 0.6F);
        RingVisuals.sound(level, to, SoundEvents.ENDERMAN_TELEPORT, 1.0F, 0.8F);
        var home = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getValue(net.minecraft.resources.Identifier.parse("theabyss:spell_of_teleport"));
        if (home != null) {
            RingVisuals.sound(level, to, home, 1.0F, 0.8F);
        }
        READY.put(player.getUUID(), now + (long)(COOLDOWN * vars.ManaCooldown));
        cast.durability(ring.getItem(), 1).charge(COST);
        return true;
    }

    private static void beacon(ServerLevel level, ServerPlayer player, Vec3 at) {
        SpellFxMessage.around(level, at, SpellFxMessage.RECALL_MARK, List.of(player.getId()), SpellFxMessage.points(at), BEACON);
    }

    private static void showMark(ServerPlayer player) {
        CompoundTag mark = player.getPersistentData().getCompoundOrEmpty(KEY);
        if (mark.contains("x") && mark.getStringOr("dim", "").equals(player.level().dimension().identifier().toString())) {
            Vec3 at = new Vec3(mark.getDoubleOr("x", 0.0), mark.getDoubleOr("y", 0.0), mark.getDoubleOr("z", 0.0));
            TheabyssMod.queueServerWork(20, () -> beacon(player.level(), player, at));
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            showMark(player);
        }
    }

    @SubscribeEvent
    public static void onChangeWorld(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            showMark(player);
        }
    }
}
