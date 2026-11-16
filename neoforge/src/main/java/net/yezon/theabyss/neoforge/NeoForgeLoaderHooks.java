package net.yezon.theabyss.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.yezon.theabyss.neoforge.blocks.NeoForgeBlockItemEvents;
import net.yezon.theabyss.neoforge.blocks.NeoForgeBlockItemHooks;
import net.yezon.theabyss.platform.services.BlockItemHooks;

final class NeoForgeLoaderHooks {
    private NeoForgeLoaderHooks() {
    }

    static void installCommon() {
        BlockItemHooks.install(new NeoForgeBlockItemHooks());
    }

    static void installClient() {
    }

    static void registerEvents(IEventBus bus) {
        NeoForgeBlockItemEvents.register(bus);
    }

    static void registerClientEvents(IEventBus bus) {
    }
}
