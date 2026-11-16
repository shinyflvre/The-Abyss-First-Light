package net.yezon.theabyss.fabric.entity;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;

public final class FabricPartsClient {
    private FabricPartsClient() {
    }

    public static void init() {
        ClientEntityEvents.ENTITY_LOAD.register(FabricParts::track);
        ClientEntityEvents.ENTITY_UNLOAD.register(FabricParts::untrack);
    }
}
