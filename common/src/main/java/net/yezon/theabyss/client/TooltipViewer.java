package net.yezon.theabyss.client;

import net.minecraft.client.Minecraft;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.fml.event.lifecycle.FMLClientSetupEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.util.AbyssTooltip;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class TooltipViewer {
    private TooltipViewer() {
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        AbyssTooltip.viewer = () -> {
            var player = Minecraft.getInstance().player;
            return player == null ? null : TheabyssModVariables.getPlayerVariables(player);
        };
    }
}
