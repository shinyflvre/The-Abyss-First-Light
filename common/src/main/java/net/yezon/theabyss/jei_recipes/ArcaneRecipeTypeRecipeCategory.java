package net.yezon.theabyss.jei_recipes;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModJeiPlugin;

public class ArcaneRecipeTypeRecipeCategory extends AbstractRecipeCategory<RecipeHolder<ArcaneRecipeTypeRecipe>> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("theabyss", "arcane_recipe_type");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/screens/arcane_station.png");
    private final IDrawable background;

    public ArcaneRecipeTypeRecipeCategory(IGuiHelper helper) {
        super(TheabyssModJeiPlugin.ArcaneRecipeType_Type, Component.translatable("jei.theabyss.category.arcane"), helper.createDrawableItemLike(TheabyssModItems.ABYSS_GUIDE.get()), 176, 98);
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 98);
    }

    @Override
    public void draw(RecipeHolder<ArcaneRecipeTypeRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ArcaneRecipeTypeRecipe> holder, IFocusGroup focuses) {
        ArcaneRecipeTypeRecipe recipe = holder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, 44, 43).addIngredients(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 43).addIngredients(recipe.getIngredients().get(1));
        builder.addSlot(RecipeIngredientRole.INPUT, 17, 16).addIngredients(recipe.getIngredients().get(2));
        builder.addSlot(RecipeIngredientRole.INPUT, 44, 7).addIngredients(recipe.getIngredients().get(3));
        builder.addSlot(RecipeIngredientRole.INPUT, 71, 16).addIngredients(recipe.getIngredients().get(4));
        builder.addSlot(RecipeIngredientRole.INPUT, 80, 43).addIngredients(recipe.getIngredients().get(5));
        builder.addSlot(RecipeIngredientRole.INPUT, 71, 70).addIngredients(recipe.getIngredients().get(6));
        builder.addSlot(RecipeIngredientRole.INPUT, 44, 79).addIngredients(recipe.getIngredients().get(7));
        builder.addSlot(RecipeIngredientRole.INPUT, 17, 70).addIngredients(recipe.getIngredients().get(8));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 152, 43).addItemStack(recipe.output());
    }
}
