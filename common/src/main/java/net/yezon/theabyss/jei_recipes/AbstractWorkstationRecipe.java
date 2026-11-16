package net.yezon.theabyss.jei_recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.mixin.accessor.NonNullListInvoker;
import org.jspecify.annotations.Nullable;

public abstract class AbstractWorkstationRecipe implements Recipe<RecipeInput> {
    private static volatile RecipeMap clientRecipes = RecipeMap.EMPTY;

    private final NonNullList<Ingredient> ingredients;
    private final ItemStackTemplate output;

    protected AbstractWorkstationRecipe(List<Ingredient> ingredients, ItemStackTemplate output) {
        this.ingredients = NonNullListInvoker.theabyss$create(List.copyOf(ingredients), null);
        this.output = output;
    }

    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public ItemStackTemplate output() {
        return this.output;
    }

    public ItemStack getResultItem(HolderLookup.@Nullable Provider registries) {
        return this.output.create();
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input) {
        return this.output.create();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    static void setClientRecipes(RecipeMap recipes) {
        clientRecipes = recipes;
    }

    public static RecipeMap clientRecipes() {
        return clientRecipes;
    }

    @FunctionalInterface
    protected interface Factory<T extends AbstractWorkstationRecipe> {
        T create(List<Ingredient> ingredients, ItemStackTemplate output);
    }

    protected static <T extends AbstractWorkstationRecipe> RecipeSerializer<T> serializer(int ingredientCount, Factory<T> factory) {
        MapCodec<T> codec = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    Ingredient.CODEC.listOf(ingredientCount, ingredientCount).fieldOf("ingredients").forGetter(AbstractWorkstationRecipe::getIngredients),
                    ItemStackTemplate.CODEC.fieldOf("output").forGetter(AbstractWorkstationRecipe::output)
                )
                .apply(i, factory::create)
        );
        StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list(ingredientCount)),
            AbstractWorkstationRecipe::getIngredients,
            ItemStackTemplate.STREAM_CODEC,
            AbstractWorkstationRecipe::output,
            factory::create
        );
        return new RecipeSerializer<>(codec, streamCodec);
    }
}
