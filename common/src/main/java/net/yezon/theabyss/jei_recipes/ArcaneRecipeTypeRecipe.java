package net.yezon.theabyss.jei_recipes;

import java.util.List;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.yezon.theabyss.init.TheabyssModRecipeTypes;

public class ArcaneRecipeTypeRecipe extends AbstractWorkstationRecipe {
    public ArcaneRecipeTypeRecipe(List<Ingredient> ingredients, ItemStackTemplate output) {
        super(ingredients, output);
    }

    @Override
    public RecipeType<ArcaneRecipeTypeRecipe> getType() {
        return TheabyssModRecipeTypes.ARCANE_RECIPE_TYPE.get();
    }

    @Override
    public RecipeSerializer<ArcaneRecipeTypeRecipe> getSerializer() {
        return TheabyssModRecipeTypes.ARCANE_RECIPE_SERIALIZER.get();
    }

    public static final class Serializer {
        public static final RecipeSerializer<ArcaneRecipeTypeRecipe> INSTANCE = serializer(9, ArcaneRecipeTypeRecipe::new);

        private Serializer() {
        }
    }
}
