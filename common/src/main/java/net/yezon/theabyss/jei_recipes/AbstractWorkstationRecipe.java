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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public abstract class AbstractWorkstationRecipe implements Recipe<RecipeInput> {
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack output;

    protected AbstractWorkstationRecipe(List<Ingredient> ingredients, ItemStack output) {
        this.ingredients = NonNullList.of(Ingredient.EMPTY, ingredients.toArray(new Ingredient[0]));
        this.output = output;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public ItemStack output() {
        return this.output;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.output.copy();
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
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
    public String getGroup() {
        return "";
    }

    @FunctionalInterface
    protected interface Factory<T extends AbstractWorkstationRecipe> {
        T create(List<Ingredient> ingredients, ItemStack output);
    }

    protected static <T extends AbstractWorkstationRecipe> RecipeSerializer<T> serializer(int ingredientCount, Factory<T> factory) {
        MapCodec<T> codec = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    Ingredient.CODEC_NONEMPTY.listOf(ingredientCount, ingredientCount).fieldOf("ingredients").forGetter(AbstractWorkstationRecipe::getIngredients),
                    ItemStack.STRICT_CODEC.fieldOf("output").forGetter(AbstractWorkstationRecipe::output)
                )
                .apply(i, factory::create)
        );
        StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list(ingredientCount)),
            AbstractWorkstationRecipe::getIngredients,
            ItemStack.STREAM_CODEC,
            AbstractWorkstationRecipe::output,
            factory::create
        );
        return new RecipeSerializer<>() {
            @Override
            public MapCodec<T> codec() {
                return codec;
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
                return streamCodec;
            }
        };
    }
}
