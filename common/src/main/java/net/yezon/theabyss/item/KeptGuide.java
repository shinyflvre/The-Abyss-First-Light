package net.yezon.theabyss.item;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.attachment.AttachmentType;
import net.yezon.theabyss.platform.event.entity.living.LivingDropsEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerEvent;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.platform.registries.NeoForgeRegistries;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModItems;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class KeptGuide {
    private static final MapCodec<List<ItemStack>> CODEC = ItemStack.CODEC.listOf().fieldOf("stacks");
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TheabyssMod.MODID);
    public static final Supplier<AttachmentType<List<ItemStack>>> TYPE = ATTACHMENTS.register(
        "kept_guide", () -> AttachmentType.<List<ItemStack>>builder(() -> List.of()).serialize(CODEC, list -> !list.isEmpty()).copyOnDeath().build()
    );

    private KeptGuide() {
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        List<ItemStack> kept = new ArrayList<>(net.yezon.theabyss.platform.attachment.Attachments.getData(player, TYPE));
        boolean any = event.getDrops().removeIf(drop -> {
            if (drop.getItem().is(TheabyssModItems.ABYSS_GUIDE.get())) {
                kept.add(drop.getItem().copy());
                return true;
            }
            return false;
        });
        if (any) {
            net.yezon.theabyss.platform.attachment.Attachments.setData(player, TYPE, List.copyOf(kept));
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || net.yezon.theabyss.platform.attachment.Attachments.getData(player, TYPE).isEmpty()) {
            return;
        }
        for (ItemStack stack : net.yezon.theabyss.platform.attachment.Attachments.getData(player, TYPE)) {
            ItemStack give = stack.copy();
            if (!player.getInventory().add(give)) {
                player.spawnAtLocation(player.level(), give);
            }
        }
        net.yezon.theabyss.platform.attachment.Attachments.setData(player, TYPE, List.of());
    }
}
