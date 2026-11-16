package net.yezon.theabyss.weather.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.weather.AbyssStorm;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class AbyssNoRainClient {
    private AbyssNoRainClient() {
    }

    @SubscribeEvent
    public static void onTickPre(ClientTickEvent.Pre event) {
        clear();
    }

    @SubscribeEvent
    public static void onTickPost(ClientTickEvent.Post event) {
        clear();
    }

    @SubscribeEvent
    public static void onFrame(RenderFrameEvent.Pre event) {
        clear();
    }

    private static void clear() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && level.dimension().equals(AbyssStorm.THE_ABYSS) && (level.getRainLevel(1.0F) > 0.0F || level.getThunderLevel(1.0F) > 0.0F)) {
            level.setRainLevel(0.0F);
            level.setThunderLevel(0.0F);
        }
    }
}
