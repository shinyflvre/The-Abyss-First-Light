package net.yezon.theabyss.ring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.RingFinisherMessage;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class RingFinisher {
    private static final int RECENT_USE = 200;
    private static final int SAME_FINISHER = 5;
    private static final List<ResourceKey<DamageType>> NOT_RING = List.of(DamageTypes.PLAYER_ATTACK, DamageTypes.SPEAR, DamageTypes.ARROW,
        DamageTypes.TRIDENT, DamageTypes.THROWN, DamageTypes.MACE_SMASH, DamageTypes.WIND_CHARGE);
    private static final Map<UUID, Long> LAST_USE = new HashMap<>();
    private static final Map<UUID, Long> LAST_SENT = new HashMap<>();

    private RingFinisher() {
    }

    private static boolean isRing(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getNamespace().equals(TheabyssMod.MODID) && id.getPath().startsWith("ring");
    }

    private static boolean armed(ItemStack stack) {
        if (isRing(stack)) {
            return true;
        }
        if (Gauntlet.is(stack)) {
            for (ItemStack ring : Gauntlet.rings(stack)) {
                if (isRing(ring)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player && armed(event.getItemStack())) {
            LAST_USE.put(player.getUUID(), player.level().getGameTime());
            TheabyssMod.LOGGER.info("[RingFinisher] ring used: {}", BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()));
        }
    }

    private static ServerPlayer owner(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            return player;
        }
        if (entity instanceof OwnableEntity ownable && ownable.getOwner() instanceof ServerPlayer player) {
            return player;
        }
        return null;
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide() || victim instanceof Player) {
            return;
        }
        DamageSource source = event.getSource();
        TheabyssMod.LOGGER.info("[RingFinisher] death of {} by {} (entity {}, direct {}, credit {})", victim.getType().getDescriptionId(),
            source.typeHolder().unwrapKey().map(k -> k.identifier().toString()).orElse("?"), source.getEntity(), source.getDirectEntity(), victim.getKillCredit());
        for (ResourceKey<DamageType> type : NOT_RING) {
            if (source.is(type)) {
                TheabyssMod.LOGGER.info("[RingFinisher] skipped: melee or vanilla weapon");
                return;
            }
        }
        ServerPlayer player = owner(source.getEntity());
        if (player == null) {
            player = owner(victim.getKillCredit());
        }
        if (player == null) {
            TheabyssMod.LOGGER.info("[RingFinisher] skipped: no player found");
            return;
        }
        long now = player.level().getGameTime();
        UUID id = player.getUUID();
        boolean ring = armed(player.getMainHandItem()) || armed(player.getOffhandItem()) || now - LAST_USE.getOrDefault(id, Long.MIN_VALUE / 2) <= RECENT_USE;
        if (!ring || now - LAST_SENT.getOrDefault(id, Long.MIN_VALUE / 2) < SAME_FINISHER) {
            TheabyssMod.LOGGER.info("[RingFinisher] skipped: ring {} (main {}, off {})", ring, player.getMainHandItem(), player.getOffhandItem());
            return;
        }
        LAST_SENT.put(id, now);
        PacketDistributor.sendToPlayer(player, new RingFinisherMessage());
        TheabyssMod.LOGGER.info("[RingFinisher] sent to {}", player.getName().getString());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_USE.remove(event.getEntity().getUUID());
        LAST_SENT.remove(event.getEntity().getUUID());
    }
}
