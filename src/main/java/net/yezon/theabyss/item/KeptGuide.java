package net.yezon.theabyss.item;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
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
        List<ItemStack> kept = new ArrayList<>(player.getData(TYPE));
        boolean any = event.getDrops().removeIf(drop -> {
            if (drop.getItem().is(TheabyssModItems.ABYSS_GUIDE.get())) {
                kept.add(drop.getItem().copy());
                return true;
            }
            return false;
        });
        if (any) {
            player.setData(TYPE, List.copyOf(kept));
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.getData(TYPE).isEmpty()) {
            return;
        }
        for (ItemStack stack : player.getData(TYPE)) {
            ItemStack give = stack.copy();
            if (!player.getInventory().add(give)) {
                player.spawnAtLocation(player.level(), give);
            }
        }
        player.setData(TYPE, List.of());
    }
}
