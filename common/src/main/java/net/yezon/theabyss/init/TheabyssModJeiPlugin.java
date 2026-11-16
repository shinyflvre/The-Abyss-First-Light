package net.yezon.theabyss.init;

import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.yezon.theabyss.jei_recipes.AbstractWorkstationRecipe;
import net.yezon.theabyss.jei_recipes.ArcaneRecipeTypeRecipe;
import net.yezon.theabyss.jei_recipes.ArcaneRecipeTypeRecipeCategory;
import net.yezon.theabyss.jei_recipes.CrystalCutterRecipeTypeRecipe;
import net.yezon.theabyss.jei_recipes.CrystalCutterRecipeTypeRecipeCategory;
import net.yezon.theabyss.jei_recipes.InfuserRecipeTypeRecipe;
import net.yezon.theabyss.jei_recipes.InfuserRecipeTypeRecipeCategory;
import net.yezon.theabyss.jei_recipes.MortarRecipeTypeRecipe;
import net.yezon.theabyss.jei_recipes.MortarRecipeTypeRecipeCategory;
import net.yezon.theabyss.jei_recipes.WorkstationRecipeSync;
import net.yezon.theabyss.mixin.accessor.RecipeManagerAccessor;

@JeiPlugin
public class TheabyssModJeiPlugin implements IModPlugin {
    public static final IRecipeHolderType<MortarRecipeTypeRecipe> MortarRecipeType_Type = IRecipeHolderType.create(MortarRecipeTypeRecipeCategory.UID);
    public static final IRecipeHolderType<InfuserRecipeTypeRecipe> InfuserRecipeType_Type = IRecipeHolderType.create(InfuserRecipeTypeRecipeCategory.UID);
    public static final IRecipeHolderType<ArcaneRecipeTypeRecipe> ArcaneRecipeType_Type = IRecipeHolderType.create(ArcaneRecipeTypeRecipeCategory.UID);
    public static final IRecipeHolderType<CrystalCutterRecipeTypeRecipe> CrystalCutterRecipeType_Type = IRecipeHolderType.create(
        CrystalCutterRecipeTypeRecipeCategory.UID
    );

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath("theabyss", "jei_plugin");
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
        registration.addRecipes(MortarRecipeType_Type, recipes(TheabyssModRecipeTypes.MORTAR_RECIPE_TYPE.get()));
        registration.addRecipes(InfuserRecipeType_Type, recipes(TheabyssModRecipeTypes.INFUSER_RECIPE_TYPE.get()));
        registration.addRecipes(ArcaneRecipeType_Type, recipes(TheabyssModRecipeTypes.ARCANE_RECIPE_TYPE.get()));
        registration.addRecipes(CrystalCutterRecipeType_Type, recipes(TheabyssModRecipeTypes.CRYSTAL_CUTTER_RECIPE_TYPE.get()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(MortarRecipeType_Type, TheabyssModBlocks.MORTAR_AND_PESTLE.get());
        registration.addCraftingStation(InfuserRecipeType_Type, TheabyssModBlocks.SOMNIUM_INFUSER.get());
        registration.addCraftingStation(ArcaneRecipeType_Type, TheabyssModBlocks.ARCANE_WORKBENCH.get());
        registration.addCraftingStation(CrystalCutterRecipeType_Type, TheabyssModBlocks.CRYSTAL_CUTTER.get());
    }

    private static <T extends AbstractWorkstationRecipe> List<RecipeHolder<T>> recipes(RecipeType<T> type) {
        RecipeMap synced = AbstractWorkstationRecipe.clientRecipes();
        if (synced.byType(type).isEmpty()) {
            IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
            if (server != null) {
                return List.copyOf(((RecipeManagerAccessor)server.getRecipeManager()).theabyss$recipes().byType(type));
            }
        }
        return List.copyOf(synced.byType(type));
    }
}
