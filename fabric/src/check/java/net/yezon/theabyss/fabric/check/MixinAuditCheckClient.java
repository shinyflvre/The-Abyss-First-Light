package net.yezon.theabyss.fabric.check;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;

public final class MixinAuditCheckClient implements ClientModInitializer {
    private static final int WORLD_TICKS = 900;
    private static int worldTicks;
    private static boolean muted;

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> MixinAuditCheck.audit("client"));
        ClientTickEvents.END_CLIENT_TICK.register(MixinAuditCheckClient::tick);
    }

    private static void tick(Minecraft client) {
        if (!muted) {
            muted = true;
            client.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(0.0);
        }
        if (client.level == null || client.player == null) {
            return;
        }
        worldTicks++;
        if (worldTicks == 1) {
            MixinAuditCheck.LOGGER.info("[ABYSS-CHECK] joined world {}", client.level.dimension().identifier());
        }
        if (worldTicks == WORLD_TICKS) {
            MixinAuditCheck.LOGGER.info("[ABYSS-CHECK] {} ticks in world, quitting", WORLD_TICKS);
            client.stop();
        }
    }
}
