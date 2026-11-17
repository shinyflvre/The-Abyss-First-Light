package net.yezon.theabyss.ring.secondary;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.player.PlayerEvent;
import net.yezon.theabyss.platform.event.tick.ServerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.fx.RingFx;
import net.yezon.theabyss.util.FallGuard;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class Rewind {
    public static final int BACK = 100;
    private static final int KEEP = 120;
    private static final int STEP = 3;
    private static final Map<UUID, Deque<Moment>> HISTORY = new HashMap<>();

    private record Moment(ResourceKey<Level> dimension, Vec3 pos, float yaw, float pitch) {
    }

    private Rewind() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (player.isSpectator()) {
                continue;
            }
            Deque<Moment> history = HISTORY.computeIfAbsent(player.getUUID(), k -> new ArrayDeque<>());
            history.addFirst(new Moment(player.level().dimension(), player.position(), player.getYRot(), player.getXRot()));
            while (history.size() > KEEP) {
                history.removeLast();
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        HISTORY.remove(event.getEntity().getUUID());
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.serverLevel();
        Deque<Moment> history = HISTORY.get(player.getUUID());
        List<Moment> path = new ArrayList<>();
        if (history != null) {
            int i = 0;
            for (Iterator<Moment> it = history.iterator(); it.hasNext() && i <= BACK; i++) {
                Moment m = it.next();
                if (m.dimension() != level.dimension()) {
                    break;
                }
                path.add(m);
            }
        }
        if (path.size() < 10 || path.get(path.size() - 1).pos().distanceToSqr(player.position()) < 0.25) {
            player.displayClientMessage(Component.translatable("ring.theabyss.time.no_past"), true);
            return false;
        }
        Moment then = path.get(path.size() - 1);
        Vec3 from = player.position();
        List<Vec3> points = new ArrayList<>();
        for (int i = 0; i < path.size(); i += STEP) {
            points.add(path.get(i).pos());
        }
        if (points.get(points.size() - 1) != then.pos()) {
            points.add(then.pos());
        }
        List<Double> data = new ArrayList<>();
        data.add((double)then.yaw());
        data.add((double)then.pitch());
        data.addAll(SpellFxMessage.points(points.toArray(new Vec3[0])));
        SpellFxMessage.around(level, from, SpellFxMessage.REWIND, List.of(player.getId()), data, 30);
        if (player.isPassenger()) {
            player.stopRiding();
        }
        net.yezon.theabyss.util.AbyssTeleport.to(player, then.pos().x, then.pos().y, then.pos().z);
        player.setYRot(then.yaw());
        player.setXRot(then.pitch());
        player.setYHeadRot(then.yaw());
        net.yezon.theabyss.util.AbyssTeleport.connection(player, then.pos().x, then.pos().y, then.pos().z, then.yaw(), then.pitch());
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        player.clearFire();
        FallGuard.protect(player, 60);
        history.clear();
        RingFx.wave(level, from.add(0.0, 0.1, 0.0), 5.0F, 16, RingVisuals.REG_GOLD);
        RingFx.wave(level, then.pos().add(0.0, 0.1, 0.0), 5.0F, 16, 0x7FE8FF);
        var timeSound = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(net.minecraft.resources.ResourceLocation.parse("theabyss:spell_of_time"));
        if (timeSound != null) {
            RingVisuals.sound(level, then.pos(), timeSound, 1.0F, 1.4F);
        }
        RingVisuals.sound(level, from, SoundEvents.BEACON_DEACTIVATE, 1.4F, 1.8F);
        RingVisuals.sound(level, then.pos(), SoundEvents.AMETHYST_BLOCK_RESONATE, 1.6F, 0.6F);
        RingVisuals.sound(level, then.pos(), SoundEvents.ILLUSIONER_PREPARE_MIRROR, 1.0F, 1.5F);
        return true;
    }
}
