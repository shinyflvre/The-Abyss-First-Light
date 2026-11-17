package net.yezon.theabyss.platform.client.event;

import net.minecraft.world.item.crafting.RecipeManager;
import net.yezon.theabyss.platform.event.Event;

public class RecipesReceivedEvent extends Event {
    private final RecipeManager recipeManager;

    public RecipesReceivedEvent(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    public RecipeManager getRecipeManager() {
        return this.recipeManager;
    }
}
