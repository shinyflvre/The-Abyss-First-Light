package net.yezon.theabyss.jei_recipes;

import java.util.List;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.yezon.theabyss.init.TheabyssModRecipeTypes;

public class MortarRecipeTypeRecipe extends AbstractWorkstationRecipe {
    public MortarRecipeTypeRecipe(List<Ingredient> ingredients, ItemStackTemplate output) {
        super(ingredients, output);
    }

    @Override
    public RecipeType<MortarRecipeTypeRecipe> getType() {
        return TheabyssModRecipeTypes.MORTAR_RECIPE_TYPE.get();
    }

    @Override
    public RecipeSerializer<MortarRecipeTypeRecipe> getSerializer() {
        return TheabyssModRecipeTypes.MORTAR_RECIPE_SERIALIZER.get();
    }

    public static final class Serializer {
        public static final RecipeSerializer<MortarRecipeTypeRecipe> INSTANCE = serializer(5, MortarRecipeTypeRecipe::new);

        private Serializer() {
        }
    }
}
