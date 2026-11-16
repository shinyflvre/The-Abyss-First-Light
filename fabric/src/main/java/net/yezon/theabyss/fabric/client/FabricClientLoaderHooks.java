package net.yezon.theabyss.fabric.client;

import net.yezon.theabyss.fabric.blocks.FabricBlockItemsClient;

final class FabricClientLoaderHooks {
    private FabricClientLoaderHooks() {
    }

    static void installHooks() {
    }

    static void install() {
        FabricBlockItemsClient.init();
    }
}
