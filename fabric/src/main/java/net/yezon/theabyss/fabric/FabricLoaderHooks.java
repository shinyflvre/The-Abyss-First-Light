package net.yezon.theabyss.fabric;

import net.yezon.theabyss.fabric.blocks.FabricBlockItems;
import net.yezon.theabyss.platform.registries.DeferredRegister;

final class FabricLoaderHooks {
    private FabricLoaderHooks() {
    }

    static void installCommon() {
        FabricBlockItems.installHooks();
    }

    static void afterRegistration() {
        FabricBlockItems.afterRegistration();
        net.yezon.theabyss.fabric.server.FabricServerSetup.afterRegistration();
    }

    static void install() {
    }

    static void registerFluidTypes(DeferredRegister<?> register) {
        register.markRegistered();
    }
}
