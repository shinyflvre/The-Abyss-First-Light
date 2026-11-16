package net.yezon.theabyss.fabric.client;

import net.yezon.theabyss.platform.services.ClientPlatformHooks;

public final class FabricClientSetup {
    private FabricClientSetup() {
    }

    public static void installHooks() {
        ClientPlatformHooks.install(new FabricClientPlatformHooks());
    }

    public static void register() {
        FabricClientRegistrations.register();
        FabricClientCommands.register();
    }
}
