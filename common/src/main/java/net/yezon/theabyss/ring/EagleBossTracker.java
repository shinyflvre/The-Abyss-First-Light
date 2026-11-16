package net.yezon.theabyss.ring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.tick.PlayerTickEvent;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.entity.AbyssaurEntity;
import net.yezon.theabyss.entity.CrystalGolemEntity;
import net.yezon.theabyss.entity.ElderEntity;
import net.yezon.theabyss.entity.MagicianEntity;
import net.yezon.theabyss.entity.NightbladeBossEntity;
import net.yezon.theabyss.entity.TheRokaEntity;
import net.yezon.theabyss.network.EagleBossesMessage;
import net.yezon.theabyss.platform.services.ServerHooks;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class EagleBossTracker {
    private static final double MAX_DISTANCE = 1024.0;
    public static final int OUTLINE = 0xB04CFF;
    private static final Map<UUID, Long> ACTIVE = new HashMap<>();

    private EagleBossTracker() {
    }

    public static boolean isBoss(LivingEntity e) {
        if (e instanceof NightbladeBossEntity nightblade) {
            return !nightblade.isMirror();
        }
        return e instanceof TheRokaEntity || e instanceof AbyssaurEntity || e instanceof MagicianEntity || e instanceof ElderEntity
            || e instanceof CrystalGolemEntity;
    }

    public static void start(ServerPlayer player, int ticks) {
        ACTIVE.put(player.getUUID(), player.level().getGameTime() + ticks);
        List<LivingEntity> bosses = find(player);
        for (LivingEntity boss : bosses) {
            boss.addEffect(new MobEffectInstance(MobEffects.GLOWING, ticks, 0, false, false));
        }
        if (bosses.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.eagle.no_boss"));
        }
        send(player, bosses, ticks);
    }

    private static List<LivingEntity> find(ServerPlayer player) {
        List<LivingEntity> out = new ArrayList<>();
        ServerLevel level = player.level();
        level.getEntities(EntityTypeTest.forClass(LivingEntity.class), e -> e.isAlive() && isBoss(e) && e.distanceToSqr(player) <= MAX_DISTANCE * MAX_DISTANCE, out);
        return out;
    }

    private static void send(ServerPlayer player, List<LivingEntity> bosses, int ticks) {
        List<EagleBossesMessage.Marker> markers = new ArrayList<>();
        for (LivingEntity boss : bosses) {
            if (markers.size() >= 64) {
                break;
            }
            markers.add(new EagleBossesMessage.Marker(BuiltInRegistries.ENTITY_TYPE.getKey(boss.getType()).toString(), boss.getX(), boss.getY() + boss.getBbHeight() * 0.5, boss.getZ()));
        }
        if (player.connection != null && ServerHooks.get().hasChannel(player.connection, EagleBossesMessage.TYPE)) {
            PacketDistributor.sendToPlayer(player, new EagleBossesMessage(markers, ticks));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || ACTIVE.isEmpty()) {
            return;
        }
        Long until = ACTIVE.get(player.getUUID());
        if (until == null) {
            return;
        }
        long now = player.level().getGameTime();
        if (now >= until) {
            ACTIVE.remove(player.getUUID());
            return;
        }
        if (now % 10 == 0) {
            send(player, find(player), (int)(until - now));
        }
    }
}
