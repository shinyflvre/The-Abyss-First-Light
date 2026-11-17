package net.yezon.theabyss.station;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.yezon.theabyss.init.TheabyssModRecipeTypes;
import net.yezon.theabyss.jei_recipes.AbstractWorkstationRecipe;
import net.yezon.theabyss.jei_recipes.ArcaneRecipeTypeRecipe;
import net.yezon.theabyss.jei_recipes.CrystalCutterRecipeTypeRecipe;
import net.yezon.theabyss.jei_recipes.InfuserRecipeTypeRecipe;
import net.yezon.theabyss.jei_recipes.MortarRecipeTypeRecipe;

public final class StationRecipes {
    private StationRecipes() {
    }

    private static <T extends AbstractWorkstationRecipe> Iterable<RecipeHolder<T>> all(ServerLevel level, RecipeType<T> type) {
        return level.getRecipeManager().getAllRecipesFor(type);
    }

    public static ArcaneRecipeTypeRecipe arcane(ServerLevel level, ItemStack[] grid) {
        for (RecipeHolder<ArcaneRecipeTypeRecipe> holder : all(level, TheabyssModRecipeTypes.ARCANE_RECIPE_TYPE.get())) {
            List<Ingredient> ing = holder.value().getIngredients();
            if (!ing.get(0).test(grid[0])) {
                continue;
            }
            for (int turn = 0; turn < 8; turn++) {
                boolean ok = true;
                for (int i = 0; i < 8 && ok; i++) {
                    ok = ing.get(1 + (i + turn) % 8).test(grid[1 + i]);
                }
                if (ok) {
                    return holder.value();
                }
            }
        }
        return null;
    }

    public static MortarRecipeTypeRecipe mortar(ServerLevel level, ItemStack[] outer, ItemStack middle) {
        for (RecipeHolder<MortarRecipeTypeRecipe> holder : all(level, TheabyssModRecipeTypes.MORTAR_RECIPE_TYPE.get())) {
            List<Ingredient> ing = holder.value().getIngredients();
            if (ing.get(4).test(middle) && shapeless(ing.subList(0, 4), outer)) {
                return holder.value();
            }
        }
        return null;
    }

    public static InfuserRecipeTypeRecipe infuser(ServerLevel level, ItemStack[] inputs, ItemStack somnium) {
        for (RecipeHolder<InfuserRecipeTypeRecipe> holder : all(level, TheabyssModRecipeTypes.INFUSER_RECIPE_TYPE.get())) {
            List<Ingredient> ing = holder.value().getIngredients();
            if (ing.get(4).test(somnium) && shapeless(ing.subList(0, 4), inputs)) {
                return holder.value();
            }
        }
        return null;
    }

    public static CrystalCutterRecipeTypeRecipe cutter(ServerLevel level, ItemStack ore) {
        if (ore.isEmpty()) {
            return null;
        }
        for (RecipeHolder<CrystalCutterRecipeTypeRecipe> holder : all(level, TheabyssModRecipeTypes.CRYSTAL_CUTTER_RECIPE_TYPE.get())) {
            if (holder.value().getIngredients().get(0).test(ore)) {
                return holder.value();
            }
        }
        return null;
    }

    private static boolean shapeless(List<Ingredient> ingredients, ItemStack[] stacks) {
        if (ingredients.size() != stacks.length) {
            return false;
        }
        return assign(ingredients, stacks, 0, new boolean[stacks.length]);
    }

    private static boolean assign(List<Ingredient> ingredients, ItemStack[] stacks, int i, boolean[] used) {
        if (i == ingredients.size()) {
            return true;
        }
        for (int s = 0; s < stacks.length; s++) {
            if (!used[s] && ingredients.get(i).test(stacks[s])) {
                used[s] = true;
                if (assign(ingredients, stacks, i + 1, used)) {
                    return true;
                }
                used[s] = false;
            }
        }
        return false;
    }
}
