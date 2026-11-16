package net.yezon.theabyss.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.yezon.theabyss.neoforge.client.NeoForgeClientPlatformHooks;
import net.yezon.theabyss.neoforge.client.NeoForgeClientRegistrations;
import net.yezon.theabyss.neoforge.fluids.NeoForgeFluidsClient;
import net.yezon.theabyss.platform.services.ClientPlatformHooks;

final class TheabyssNeoForgeClient {
    private TheabyssNeoForgeClient() {
    }

    static void installHooks() {
        ClientPlatformHooks.install(new NeoForgeClientPlatformHooks());
        NeoForgeLoaderHooks.installClient();
    }

    static void init(IEventBus bus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        NeoForgeClientRegistrations.register(bus);
        NeoForgeFluidsClient.register(bus);
        NeoForgeLoaderHooks.registerClientEvents(bus);
    }
}
