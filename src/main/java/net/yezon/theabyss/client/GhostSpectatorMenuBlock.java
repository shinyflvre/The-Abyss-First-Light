package net.yezon.theabyss.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModMobEffects;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class GhostSpectatorMenuBlock {
    private GhostSpectatorMenuBlock() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !player.isSpectator() || !player.hasEffect(TheabyssModMobEffects.GHOST)) {
            return;
        }
        Options options = mc.options;
        for (var key : options.keyHotbarSlots) {
            while (key.consumeClick()) {
            }
        }
        while (options.keySpectatorHotbar.consumeClick()) {
        }
    }
}
