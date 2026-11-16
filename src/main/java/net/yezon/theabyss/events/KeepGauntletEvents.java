package net.yezon.theabyss.events;

import java.util.Iterator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.ring.Gauntlet;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class KeepGauntletEvents {
    private static final String KEPT = "theabyss_kept_gauntlets";

    private KeepGauntletEvents() {
    }

    private static RegistryOps<Tag> ops(ServerPlayer player) {
        return player.registryAccess().createSerializationContext(NbtOps.INSTANCE);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        CompoundTag data = player.getPersistentData();
        ListTag kept = data.getListOrEmpty(KEPT);
        Iterator<ItemEntity> drops = event.getDrops().iterator();
        while (drops.hasNext()) {
            ItemStack stack = drops.next().getItem();
            if (Gauntlet.is(stack)) {
                ItemStack.CODEC.encodeStart(ops(player), stack).result().ifPresent(kept::add);
                drops.remove();
            }
        }
        if (!kept.isEmpty()) {
            data.put(KEPT, kept);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        Tag kept = event.getOriginal().getPersistentData().get(KEPT);
        if (kept != null) {
            event.getEntity().getPersistentData().put(KEPT, kept.copy());
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.getPersistentData().contains(KEPT)) {
            return;
        }
        ListTag kept = player.getPersistentData().getListOrEmpty(KEPT);
        player.getPersistentData().remove(KEPT);
        for (Tag tag : kept) {
            ItemStack.CODEC.parse(ops(player), tag).result().ifPresent(stack -> player.getInventory().placeItemBackInInventory(stack, net.minecraft.util.Prediction.SERVER_ONLY));
        }
    }
}
