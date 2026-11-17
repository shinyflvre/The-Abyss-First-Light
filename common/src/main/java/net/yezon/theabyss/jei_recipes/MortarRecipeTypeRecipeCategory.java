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

public class MortarRecipeTypeRecipeCategory extends AbstractRecipeCategory<RecipeHolder<MortarRecipeTypeRecipe>> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("theabyss", "mortar_recipe_type");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/screens/mortar_jei.png");
    private final IDrawable background;

    public MortarRecipeTypeRecipeCategory(IGuiHelper helper) {
        super(TheabyssModJeiPlugin.MortarRecipeType_Type, Component.translatable("jei.theabyss.category.mortar"), helper.createDrawableItemLike(TheabyssModItems.ABYSS_GUIDE.get()), 176, 98);
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 98);
    }

    @Override
    public void draw(RecipeHolder<MortarRecipeTypeRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<MortarRecipeTypeRecipe> holder, IFocusGroup focuses) {
        MortarRecipeTypeRecipe recipe = holder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, 44, 25).addIngredients(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.INPUT, 26, 43).addIngredients(recipe.getIngredients().get(1));
        builder.addSlot(RecipeIngredientRole.INPUT, 44, 43).addIngredients(recipe.getIngredients().get(4));
        builder.addSlot(RecipeIngredientRole.INPUT, 62, 43).addIngredients(recipe.getIngredients().get(3));
        builder.addSlot(RecipeIngredientRole.INPUT, 44, 61).addIngredients(recipe.getIngredients().get(2));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 134, 43).addItemStack(recipe.output());
    }
}
