package net.yezon.theabyss.neoforge.event;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;

public final class NeoForgeEventBridge {
    private NeoForgeEventBridge() {
    }

    public static void register(IEventBus bus) {
        NeoForgeEventBridgeGenerated.register(bus);
        if (FMLEnvironment.getDist().isClient()) {
            NeoForgeClientEventBridgeGenerated.register(bus);
        }
    }
}
