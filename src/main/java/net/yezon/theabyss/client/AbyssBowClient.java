package net.yezon.theabyss.client;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.item.AbyssBowItem;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class AbyssBowClient {
    private AbyssBowClient() {
    }

    @SubscribeEvent
    public static void onFov(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
        if (!player.isUsingItem() || !(player.getUseItem().getItem() instanceof AbyssBowItem)) {
            return;
        }
        float scale = Math.min(player.getTicksUsingItem() / 20.0F, 1.0F);
        event.setNewFovModifier(event.getNewFovModifier() * (1.0F - Mth.square(scale) * 0.15F));
    }
}
