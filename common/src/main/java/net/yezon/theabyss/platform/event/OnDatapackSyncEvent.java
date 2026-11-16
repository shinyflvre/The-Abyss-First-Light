package net.yezon.theabyss.platform.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class OnDatapackSyncEvent extends Event {
    public abstract void sendRecipes(net.minecraft.world.item.crafting.RecipeType<?>... a0);
}
