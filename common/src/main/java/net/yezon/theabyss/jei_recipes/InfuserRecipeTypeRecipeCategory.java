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

public class InfuserRecipeTypeRecipeCategory extends AbstractRecipeCategory<RecipeHolder<InfuserRecipeTypeRecipe>> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("theabyss", "infuser_recipe_type");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/screens/somnium_infuser.png");
    private final IDrawable background;

    public InfuserRecipeTypeRecipeCategory(IGuiHelper helper) {
        super(TheabyssModJeiPlugin.InfuserRecipeType_Type, Component.translatable("jei.theabyss.category.infuser"), helper.createDrawableItemLike(TheabyssModItems.ABYSS_GUIDE.get()), 176, 98);
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 98);
    }

    @Override
    public void draw(RecipeHolder<InfuserRecipeTypeRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<InfuserRecipeTypeRecipe> holder, IFocusGroup focuses) {
        InfuserRecipeTypeRecipe recipe = holder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, 27, 42).addIngredients(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.INPUT, 53, 50).addIngredients(recipe.getIngredients().get(1));
        builder.addSlot(RecipeIngredientRole.INPUT, 105, 50).addIngredients(recipe.getIngredients().get(2));
        builder.addSlot(RecipeIngredientRole.INPUT, 131, 42).addIngredients(recipe.getIngredients().get(3));
        builder.addSlot(RecipeIngredientRole.INPUT, 35, 7).addIngredients(recipe.getIngredients().get(4));
        builder.addSlot(RecipeIngredientRole.INPUT, 79, 16).addIngredients(recipe.getIngredients().get(5));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 79, 59).addItemStack(recipe.output());
    }
}
