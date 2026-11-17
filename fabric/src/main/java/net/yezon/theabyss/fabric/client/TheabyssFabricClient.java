package net.yezon.theabyss.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.yezon.theabyss.client.TheabyssClientSubscribers;
import net.yezon.theabyss.fabric.entity.FabricPartsClient;
import net.yezon.theabyss.fabric.event.FabricClientEventBridge;
import net.yezon.theabyss.fabric.fluids.FabricFluidsClient;
import net.yezon.theabyss.platform.event.AbyssEventBus;

public final class TheabyssFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricClientSetup.installHooks();
        FabricGuiRegistrations.installHooks();
        FabricClientLoaderHooks.installHooks();
        for (Class<?> subscriber : TheabyssClientSubscribers.ALL) {
            AbyssEventBus.register(subscriber);
        }
        FabricPartsClient.init();
        FabricClientNetwork.register();
        FabricClientEventBridge.register();
        FabricClientSetup.register();
        FabricGuiRegistrations.register();
        FabricRenderRegistrations.register();
        FabricFluidsClient.install();
        FabricClientLoaderHooks.install();
    }
}
