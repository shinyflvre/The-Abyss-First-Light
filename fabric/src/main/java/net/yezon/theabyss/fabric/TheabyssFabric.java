package net.yezon.theabyss.fabric;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import java.util.List;
import net.fabricmc.api.ModInitializer;
import net.neoforged.fml.config.ModConfig;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.configuration.ConfigConfiguration;
import net.yezon.theabyss.fabric.core.FabricNetwork;
import net.yezon.theabyss.fabric.core.FabricPlatform;
import net.yezon.theabyss.fabric.core.FabricRegistration;
import net.yezon.theabyss.fabric.entity.FabricEntityHooks;
import net.yezon.theabyss.fabric.entity.FabricEntityRegistration;
import net.yezon.theabyss.fabric.entity.FabricParts;
import net.yezon.theabyss.fabric.entity.FabricPersistentData;
import net.yezon.theabyss.fabric.event.FabricEventBridge;
import net.yezon.theabyss.fabric.fluids.FabricFluids;
import net.yezon.theabyss.fabric.server.FabricServerSetup;
import net.yezon.theabyss.platform.Platform;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.platform.services.EntityHooks;

public final class TheabyssFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Platform.install(new FabricPlatform());
        FabricServerSetup.install();
        EntityHooks.install(new FabricEntityHooks());
        FabricPlatform.setPersistentData(FabricPersistentData::get);
        FabricLoaderHooks.installCommon();
        FabricParts.init();
        List<DeferredRegister<?>> registers = TheabyssMod.init();
        FabricEventBridge.beforeRegistration();
        FabricRegistration.register(registers, FabricLoaderHooks::registerFluidTypes);
        FabricLoaderHooks.afterRegistration();
        FabricEntityRegistration.registerAttributes();
        FabricEntityRegistration.registerSpawnPlacements();
        FabricNetwork.registerCommon();
        FabricEventBridge.register();
        FabricFluids.install();
        FabricLoaderHooks.install();
        NeoForgeConfigRegistry.INSTANCE.register(TheabyssMod.MODID, ModConfig.Type.COMMON, ConfigConfiguration.SPEC, "ta_theotherside.toml");
    }
}
