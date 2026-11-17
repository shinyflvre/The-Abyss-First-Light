package net.yezon.theabyss.events;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.living.LivingDeathEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.entity.PlayerBodyEntity;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModMobEffects;
import net.yezon.theabyss.network.AfterLifeFxMessage;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class AfterLifeHandlerEvents {
    public static final int DURATION = 800;
    private static final String MODE_KEY = "theabyss_afterlife_mode";
    private static final double SPREAD = 128.0;
    private static final double REACH = 2.0;
    private static final double NEARBY = 128.0;
    private static final int RETURN_DELAY = 20;
    private static final int MESSAGE_INTERVAL = 20;
    private static final float GHOST_HEALTH = 10.0F;
    private static final float REVIVED_HEALTH = 20.0F;
    private static final float FINAL_DAMAGE = 100.0F;
    private static final Set<UUID> FOUND = ConcurrentHashMap.newKeySet();

    private AfterLifeHandlerEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!net.yezon.theabyss.rift.Blessings.useUndying(player)) {
            ItemStack artifact = findArtifact(player.getInventory());
            if (artifact == null) {
                return;
            }
            artifact.shrink(1);
        }
        event.setCanceled(true);
        Vec3 deathAt = player.position();
        player.setHealth(GHOST_HEALTH);
        rememberGameMode(player);
        player.addEffect(new MobEffectInstance(TheabyssModMobEffects.AFTER_LIFE.getDelegate(), DURATION, 1, false, false));
        net.yezon.theabyss.util.AbyssTeleport.to(player, deathAt.x + player.getRandom().nextDouble() * SPREAD, deathAt.y, deathAt.z + player.getRandom().nextDouble() * SPREAD);
        PlayerBodyEntity body = TheabyssModEntities.PLAYER_BODY.get().spawn(player.serverLevel(), BlockPos.containing(deathAt), MobSpawnType.MOB_SUMMONED);
        if (body != null) {
            body.setYRot(player.getRandom().nextFloat() * 360.0F);
        }
        AfterLifeFxMessage.send(player, AfterLifeFxMessage.START, body != null ? body.position() : deathAt, DURATION);
    }

    public static void started(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            rememberGameMode(player);
            player.setGameMode(GameType.SPECTATOR);
        }
    }

    public static void tick(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player) || FOUND.contains(player.getUUID())) {
            return;
        }
        if (bodyWithin(player, REACH)) {
            FOUND.add(player.getUUID());
            AfterLifeFxMessage.send(player, AfterLifeFxMessage.FOUND, player.position(), 0);
            player.addEffect(new MobEffectInstance(TheabyssModMobEffects.TANTRUM.getDelegate(), 200, 1, false, false));
            player.removeEffect(TheabyssModMobEffects.AFTER_LIFE.getDelegate());
            return;
        }
        if (player.tickCount % MESSAGE_INTERVAL == 0 && bodyWithin(player, NEARBY)) {
            player.displayClientMessage(Component.translatable("message.theabyss.body_nearby"), true);
        }
    }

    public static void ended(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        boolean found = FOUND.remove(player.getUUID());
        if (!found) {
            AfterLifeFxMessage.send(player, AfterLifeFxMessage.END, player.position(), 0);
        }
        TheabyssMod.queueServerWork(RETURN_DELAY, () -> {
            restoreGameMode(player);
            if (found) {
                player.setHealth(REVIVED_HEALTH);
            } else {
                player.hurt(player.damageSources().generic(), FINAL_DAMAGE);
            }
        });
    }

    private static ItemStack findArtifact(Inventory inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(TheabyssModItems.ARTIFACT_OF_AFTER_LIFE.get())) {
                return stack;
            }
        }
        return null;
    }

    private static boolean bodyWithin(ServerPlayer player, double range) {
        return !player.level().getEntitiesOfClass(PlayerBodyEntity.class, new AABB(player.position(), player.position()).inflate(range)).isEmpty();
    }

    private static void rememberGameMode(ServerPlayer player) {
        if (!net.yezon.theabyss.platform.PersistentData.get(player).contains(MODE_KEY) && player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
            net.yezon.theabyss.platform.PersistentData.get(player).putString(MODE_KEY, player.gameMode.getGameModeForPlayer().getName());
        }
    }

    private static void restoreGameMode(ServerPlayer player) {
        GameType mode = GameType.byName(net.yezon.theabyss.compat.NbtCompat.getStringOr(net.yezon.theabyss.platform.PersistentData.get(player), MODE_KEY, GameType.SURVIVAL.getName()), GameType.SURVIVAL);
        net.yezon.theabyss.platform.PersistentData.get(player).remove(MODE_KEY);
        player.setGameMode(mode);
    }
}
