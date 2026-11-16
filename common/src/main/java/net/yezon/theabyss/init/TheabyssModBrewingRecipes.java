package net.yezon.theabyss.init;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import net.minecraft.resources.Identifier;

@JeiPlugin
public class TheabyssModBrewingRecipes implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath("theabyss", "brewing_recipes");
    }
}
