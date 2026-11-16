package net.yezon.theabyss.jei_recipes;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModJeiPlugin;

public class CrystalCutterRecipeTypeRecipeCategory extends AbstractRecipeCategory<RecipeHolder<CrystalCutterRecipeTypeRecipe>> {
    public static final Identifier UID = Identifier.fromNamespaceAndPath("theabyss", "crystal_cutter_recipe_type");
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("theabyss", "textures/screens/jei_crystalcutter_gui.png");
    private final IDrawable background;

    public CrystalCutterRecipeTypeRecipeCategory(IGuiHelper helper) {
        super(
            TheabyssModJeiPlugin.CrystalCutterRecipeType_Type,
            Component.translatable("jei.theabyss.category.crystal_cutter"),
            helper.createDrawableItemLike(TheabyssModItems.ABYSS_GUIDE.get()),
            176,
            98
        );
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 98);
    }

    @Override
    public void draw(RecipeHolder<CrystalCutterRecipeTypeRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CrystalCutterRecipeTypeRecipe> holder, IFocusGroup focuses) {
        CrystalCutterRecipeTypeRecipe recipe = holder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, 26, 53).add(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.INPUT, 80, 26).add(recipe.getIngredients().get(1));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 134, 53).add(recipe.output());
    }
}
