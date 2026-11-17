package net.yezon.theabyss.fabric.event;

import net.minecraft.world.item.crafting.RecipeManager;
import net.yezon.theabyss.platform.client.event.RecipesReceivedEvent;
import net.yezon.theabyss.platform.event.AbyssEventBus;

public final class ServerFabricEventsClient {
    private ServerFabricEventsClient() {
    }

    static void register() {
    }

    public static void recipesUpdated(RecipeManager recipes) {
        if (AbyssEventBus.hasListeners(RecipesReceivedEvent.class)) {
            AbyssEventBus.post(new RecipesReceivedEvent(recipes));
        }
    }
}
