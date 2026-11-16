package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public class RecipesReceivedEvent extends Event {
    private final net.minecraft.world.item.crafting.RecipeMap recipeMap;

    public RecipesReceivedEvent(net.minecraft.world.item.crafting.RecipeMap recipeMap) {
        this.recipeMap = recipeMap;
    }

    public net.minecraft.world.item.crafting.RecipeMap getRecipeMap() {
        return this.recipeMap;
    }
}
