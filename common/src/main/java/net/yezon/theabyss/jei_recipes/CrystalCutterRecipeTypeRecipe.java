package net.yezon.theabyss.jei_recipes;

import java.util.List;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.yezon.theabyss.init.TheabyssModRecipeTypes;

public class CrystalCutterRecipeTypeRecipe extends AbstractWorkstationRecipe {
    public CrystalCutterRecipeTypeRecipe(List<Ingredient> ingredients, ItemStackTemplate output) {
        super(ingredients, output);
    }

    @Override
    public RecipeType<CrystalCutterRecipeTypeRecipe> getType() {
        return TheabyssModRecipeTypes.CRYSTAL_CUTTER_RECIPE_TYPE.get();
    }

    @Override
    public RecipeSerializer<CrystalCutterRecipeTypeRecipe> getSerializer() {
        return TheabyssModRecipeTypes.CRYSTAL_CUTTER_RECIPE_SERIALIZER.get();
    }

    public static final class Serializer {
        public static final RecipeSerializer<CrystalCutterRecipeTypeRecipe> INSTANCE = serializer(2, CrystalCutterRecipeTypeRecipe::new);

        private Serializer() {
        }
    }
}
