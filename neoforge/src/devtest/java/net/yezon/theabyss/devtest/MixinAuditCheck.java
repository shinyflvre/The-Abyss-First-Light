package net.yezon.theabyss.devtest;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.spongepowered.asm.mixin.MixinEnvironment;

public final class MixinAuditCheck {
    private static boolean clientDone;

    private MixinAuditCheck() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (!FMLEnvironment.dist.isClient()) {
            audit("server");
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!clientDone && Minecraft.getInstance().level != null) {
            clientDone = true;
            audit("client");
        }
    }

    private static void audit(String side) {
        try {
            MixinEnvironment.getCurrentEnvironment().audit();
            TheabyssDevTest.LOGGER.info("[ABYSS-CHECK] mixin audit {} finished", side);
        } catch (Throwable t) {
            TheabyssDevTest.LOGGER.error("[ABYSS-CHECK] mixin audit {} FAILED", side, t);
        }
    }
}
