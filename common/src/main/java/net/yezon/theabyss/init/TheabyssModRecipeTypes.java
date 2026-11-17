package net.yezon.theabyss.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.jei_recipes.ArcaneRecipeTypeRecipe;
import net.yezon.theabyss.jei_recipes.CrystalCutterRecipeTypeRecipe;
import net.yezon.theabyss.jei_recipes.InfuserRecipeTypeRecipe;
import net.yezon.theabyss.jei_recipes.MortarRecipeTypeRecipe;

public class TheabyssModRecipeTypes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, TheabyssMod.MODID);
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, TheabyssMod.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<MortarRecipeTypeRecipe>> MORTAR_RECIPE_TYPE = type("mortar_recipe_type");
    public static final DeferredHolder<RecipeType<?>, RecipeType<InfuserRecipeTypeRecipe>> INFUSER_RECIPE_TYPE = type("infuser_recipe_type");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ArcaneRecipeTypeRecipe>> ARCANE_RECIPE_TYPE = type("arcane_recipe_type");
    public static final DeferredHolder<RecipeType<?>, RecipeType<CrystalCutterRecipeTypeRecipe>> CRYSTAL_CUTTER_RECIPE_TYPE = type("crystal_cutter_recipe_type");

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MortarRecipeTypeRecipe>> MORTAR_RECIPE_SERIALIZER = SERIALIZERS.register(
        "mortar_recipe_type", () -> MortarRecipeTypeRecipe.Serializer.INSTANCE
    );
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<InfuserRecipeTypeRecipe>> INFUSER_RECIPE_SERIALIZER = SERIALIZERS.register(
        "infuser_recipe_type", () -> InfuserRecipeTypeRecipe.Serializer.INSTANCE
    );
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ArcaneRecipeTypeRecipe>> ARCANE_RECIPE_SERIALIZER = SERIALIZERS.register(
        "arcane_recipe_type", () -> ArcaneRecipeTypeRecipe.Serializer.INSTANCE
    );
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CrystalCutterRecipeTypeRecipe>> CRYSTAL_CUTTER_RECIPE_SERIALIZER = SERIALIZERS.register(
        "crystal_cutter_recipe_type", () -> CrystalCutterRecipeTypeRecipe.Serializer.INSTANCE
    );

    private static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> type(String name) {
        String id = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, name).toString();
        return TYPES.register(name, () -> new RecipeType<T>() {
            @Override
            public String toString() {
                return id;
            }
        });
    }
}
