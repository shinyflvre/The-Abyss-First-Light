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

public class ArcaneRecipeTypeRecipeCategory extends AbstractRecipeCategory<RecipeHolder<ArcaneRecipeTypeRecipe>> {
    public static final Identifier UID = Identifier.fromNamespaceAndPath("theabyss", "arcane_recipe_type");
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("theabyss", "textures/screens/arcane_station.png");
    private final IDrawable background;

    public ArcaneRecipeTypeRecipeCategory(IGuiHelper helper) {
        super(TheabyssModJeiPlugin.ArcaneRecipeType_Type, Component.translatable("jei.theabyss.category.arcane"), helper.createDrawableItemLike(TheabyssModItems.ABYSS_GUIDE.get()), 176, 98);
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 98);
    }

    @Override
    public void draw(RecipeHolder<ArcaneRecipeTypeRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ArcaneRecipeTypeRecipe> holder, IFocusGroup focuses) {
        ArcaneRecipeTypeRecipe recipe = holder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, 44, 43).add(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 43).add(recipe.getIngredients().get(1));
        builder.addSlot(RecipeIngredientRole.INPUT, 17, 16).add(recipe.getIngredients().get(2));
        builder.addSlot(RecipeIngredientRole.INPUT, 44, 7).add(recipe.getIngredients().get(3));
        builder.addSlot(RecipeIngredientRole.INPUT, 71, 16).add(recipe.getIngredients().get(4));
        builder.addSlot(RecipeIngredientRole.INPUT, 80, 43).add(recipe.getIngredients().get(5));
        builder.addSlot(RecipeIngredientRole.INPUT, 71, 70).add(recipe.getIngredients().get(6));
        builder.addSlot(RecipeIngredientRole.INPUT, 44, 79).add(recipe.getIngredients().get(7));
        builder.addSlot(RecipeIngredientRole.INPUT, 17, 70).add(recipe.getIngredients().get(8));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 152, 43).add(recipe.output());
    }
}
