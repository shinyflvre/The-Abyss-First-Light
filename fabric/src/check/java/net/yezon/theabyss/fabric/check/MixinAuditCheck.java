package net.yezon.theabyss.fabric.check;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;

public final class MixinAuditCheck implements ModInitializer {
    static final Logger LOGGER = LogManager.getLogger("theabyss_check");

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> audit("server"));
    }

    static void audit(String side) {
        try {
            MixinEnvironment.getCurrentEnvironment().audit();
            LOGGER.info("[ABYSS-CHECK] mixin audit {} finished", side);
        } catch (Throwable t) {
            LOGGER.error("[ABYSS-CHECK] mixin audit {} FAILED", side, t);
        }
    }
}
