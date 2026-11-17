package net.yezon.theabyss.neoforge;

import java.util.List;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.TheabyssClientSubscribers;
import net.yezon.theabyss.configuration.ConfigConfiguration;
import net.yezon.theabyss.neoforge.core.NeoForgeNetwork;
import net.yezon.theabyss.neoforge.core.NeoForgePlatform;
import net.yezon.theabyss.neoforge.core.NeoForgeRegistration;
import net.yezon.theabyss.neoforge.entity.NeoForgeEntityEvents;
import net.yezon.theabyss.neoforge.entity.NeoForgeEntityHooks;
import net.yezon.theabyss.neoforge.event.NeoForgeEventBridge;
import net.yezon.theabyss.neoforge.fluids.NeoForgeFluidTypes;
import net.yezon.theabyss.neoforge.server.NeoForgeServerHooks;
import net.yezon.theabyss.platform.Platform;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.platform.services.EntityHooks;
import net.yezon.theabyss.platform.services.ServerHooks;

@Mod(TheabyssMod.MODID)
public final class TheabyssNeoForge {
    public TheabyssNeoForge(IEventBus bus, ModContainer container) {
        boolean client = FMLEnvironment.dist.isClient();
        Platform.install(new NeoForgePlatform());
        ServerHooks.install(new NeoForgeServerHooks());
        EntityHooks.install(new NeoForgeEntityHooks());
        NeoForgeLoaderHooks.installCommon();
        if (client) {
            TheabyssNeoForgeClient.installHooks();
        }
        List<DeferredRegister<?>> registers = TheabyssMod.init();
        if (client) {
            for (Class<?> subscriber : TheabyssClientSubscribers.ALL) {
                AbyssEventBus.register(subscriber);
            }
        }
        NeoForgeRegistration.register(registers, bus, NeoForgeFluidTypes::register);
        bus.addListener(NeoForgeNetwork::onRegisterPayloads);
        NeoForgeEventBridge.register(bus);
        NeoForgeEntityEvents.register(bus);
        NeoForgeLoaderHooks.registerEvents(bus);
        if (client) {
            TheabyssNeoForgeClient.init(bus, container);
        }
        container.registerConfig(ModConfig.Type.COMMON, ConfigConfiguration.SPEC, "ta_theotherside.toml");
    }
}
