package net.yezon.theabyss.init;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.yezon.theabyss.jei_recipes.ArcaneRecipeTypeRecipe;
import net.yezon.theabyss.jei_recipes.ArcaneRecipeTypeRecipeCategory;
import net.yezon.theabyss.jei_recipes.CrystalCutterRecipeTypeRecipe;
import net.yezon.theabyss.jei_recipes.CrystalCutterRecipeTypeRecipeCategory;
import net.yezon.theabyss.jei_recipes.InfuserRecipeTypeRecipe;
import net.yezon.theabyss.jei_recipes.InfuserRecipeTypeRecipeCategory;
import net.yezon.theabyss.jei_recipes.MortarRecipeTypeRecipe;
import net.yezon.theabyss.jei_recipes.MortarRecipeTypeRecipeCategory;
import net.yezon.theabyss.jei_recipes.WorkstationRecipeSync;

@JeiPlugin
public class TheabyssModJeiPlugin implements IModPlugin {
    public static final RecipeType<RecipeHolder<MortarRecipeTypeRecipe>> MortarRecipeType_Type = RecipeType.createRecipeHolderType(MortarRecipeTypeRecipeCategory.UID);
    public static final RecipeType<RecipeHolder<InfuserRecipeTypeRecipe>> InfuserRecipeType_Type = RecipeType.createRecipeHolderType(InfuserRecipeTypeRecipeCategory.UID);
    public static final RecipeType<RecipeHolder<ArcaneRecipeTypeRecipe>> ArcaneRecipeType_Type = RecipeType.createRecipeHolderType(ArcaneRecipeTypeRecipeCategory.UID);
    public static final RecipeType<RecipeHolder<CrystalCutterRecipeTypeRecipe>> CrystalCutterRecipeType_Type = RecipeType.createRecipeHolderType(
        CrystalCutterRecipeTypeRecipeCategory.UID
    );

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("theabyss", "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
            new MortarRecipeTypeRecipeCategory(guiHelper),
            new InfuserRecipeTypeRecipeCategory(guiHelper),
            new ArcaneRecipeTypeRecipeCategory(guiHelper),
            new CrystalCutterRecipeTypeRecipeCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(MortarRecipeType_Type, WorkstationRecipeSync.Client.recipes(TheabyssModRecipeTypes.MORTAR_RECIPE_TYPE.get()));
        registration.addRecipes(InfuserRecipeType_Type, WorkstationRecipeSync.Client.recipes(TheabyssModRecipeTypes.INFUSER_RECIPE_TYPE.get()));
        registration.addRecipes(ArcaneRecipeType_Type, WorkstationRecipeSync.Client.recipes(TheabyssModRecipeTypes.ARCANE_RECIPE_TYPE.get()));
        registration.addRecipes(CrystalCutterRecipeType_Type, WorkstationRecipeSync.Client.recipes(TheabyssModRecipeTypes.CRYSTAL_CUTTER_RECIPE_TYPE.get()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(MortarRecipeType_Type, TheabyssModBlocks.MORTAR_AND_PESTLE.get());
        registration.addRecipeCatalysts(InfuserRecipeType_Type, TheabyssModBlocks.SOMNIUM_INFUSER.get());
        registration.addRecipeCatalysts(ArcaneRecipeType_Type, TheabyssModBlocks.ARCANE_WORKBENCH.get());
        registration.addRecipeCatalysts(CrystalCutterRecipeType_Type, TheabyssModBlocks.CRYSTAL_CUTTER.get());
    }
}
