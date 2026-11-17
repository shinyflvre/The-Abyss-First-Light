package net.yezon.theabyss.jei_recipes;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.yezon.theabyss.init.TheabyssModRecipeTypes;

public class InfuserRecipeTypeRecipe extends AbstractWorkstationRecipe {
    public InfuserRecipeTypeRecipe(List<Ingredient> ingredients, ItemStack output) {
        super(ingredients, output);
    }

    @Override
    public RecipeType<InfuserRecipeTypeRecipe> getType() {
        return TheabyssModRecipeTypes.INFUSER_RECIPE_TYPE.get();
    }

    @Override
    public RecipeSerializer<InfuserRecipeTypeRecipe> getSerializer() {
        return TheabyssModRecipeTypes.INFUSER_RECIPE_SERIALIZER.get();
    }

    public static final class Serializer {
        public static final RecipeSerializer<InfuserRecipeTypeRecipe> INSTANCE = serializer(6, InfuserRecipeTypeRecipe::new);

        private Serializer() {
        }
    }
}
