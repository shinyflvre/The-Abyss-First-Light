package net.yezon.theabyss.fabric.event;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.yezon.theabyss.fabric.mixin.event.server.RecipeMapInvoker;
import net.yezon.theabyss.platform.client.event.RecipesReceivedEvent;
import net.yezon.theabyss.platform.event.AbyssEventBus;

final class ServerFabricEventsClient {
    private ServerFabricEventsClient() {
    }

    static void register() {
        ClientPlayNetworking.registerGlobalReceiver(FabricRecipeContentPayload.TYPE, (payload, context) -> {
            if (AbyssEventBus.hasListeners(RecipesReceivedEvent.class)) {
                AbyssEventBus.post(new RecipesReceivedEvent(recipeMap(payload.recipes())));
            }
        });
    }

    private static RecipeMap recipeMap(List<RecipeHolder<?>> recipes) {
        ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byType = ImmutableMultimap.builder();
        ImmutableMap.Builder<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey = ImmutableMap.builder();
        for (RecipeHolder<?> recipe : recipes) {
            byType.put(recipe.value().getType(), recipe);
            byKey.put(recipe.id(), recipe);
        }
        return RecipeMapInvoker.theabyss$create(byType.build(), byKey.build());
    }
}
