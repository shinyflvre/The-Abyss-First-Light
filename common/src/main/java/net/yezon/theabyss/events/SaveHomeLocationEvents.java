package net.yezon.theabyss.events;

import net.minecraft.server.level.ServerPlayer;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.player.PlayerWakeUpEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.TheabyssModVariables;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class SaveHomeLocationEvents {
    private SaveHomeLocationEvents() {
    }

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
        vars.HomeX = player.getX();
        vars.HomeY = player.getY();
        vars.HomeZ = player.getZ();
        vars.RingOfHome = true;
        vars.syncPlayerVariables(player);
    }
}
