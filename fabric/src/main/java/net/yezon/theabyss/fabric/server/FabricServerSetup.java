package net.yezon.theabyss.fabric.server;

import java.util.LinkedHashSet;
import java.util.Set;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.fabric.mixin.server.PoiTypesInvoker;
import net.yezon.theabyss.platform.services.ServerHooks;

public final class FabricServerSetup {
    private FabricServerSetup() {
    }

    public static void install() {
        ServerHooks.install(new FabricServerHooks());
        FabricAttributes.bootstrap();
        RegistryEntryAddedCallback.allEntries(BuiltInRegistries.POINT_OF_INTEREST_TYPE, FabricServerSetup::mapPoiStates);
    }

    public static void afterRegistration() {
        FabricBiomeModifiers.register();
        FabricMonsterRoomMobs.load();
    }

    private static void mapPoiStates(Holder.Reference<PoiType> type) {
        if (!type.key().location().getNamespace().equals(TheabyssMod.MODID)) {
            return;
        }
        Set<BlockState> states = new LinkedHashSet<>();
        for (BlockState state : type.value().matchingStates()) {
            if (!PoiTypes.hasPoi(state)) {
                states.add(state);
            }
        }
        if (!states.isEmpty()) {
            PoiTypesInvoker.theabyss$registerBlockStates(type, states);
        }
    }
}
