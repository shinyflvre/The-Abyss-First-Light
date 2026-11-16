package net.yezon.theabyss.fabric.event;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.yezon.theabyss.TheabyssMod;

public record FabricRecipeContentPayload(Set<RecipeType<?>> recipeTypes, List<RecipeHolder<?>> recipes) implements CustomPacketPayload {
    public static final int MAX_SIZE = 64 * 1024 * 1024;
    public static final Type<FabricRecipeContentPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "recipe_content"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FabricRecipeContentPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.registry(Registries.RECIPE_TYPE).apply(ByteBufCodecs.collection(HashSet::new)), FabricRecipeContentPayload::recipeTypes,
        RecipeHolder.STREAM_CODEC.apply(ByteBufCodecs.list()), FabricRecipeContentPayload::recipes,
        FabricRecipeContentPayload::new
    );

    @Override
    public Type<FabricRecipeContentPayload> type() {
        return TYPE;
    }
}
