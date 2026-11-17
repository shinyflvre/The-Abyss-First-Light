package net.yezon.theabyss.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.yezon.theabyss.TheabyssMod;

public final class AbyssBeeFlowers {
    public static final TagKey<Block> BEE_ATTRACTIVE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "bee_attractive"));
    public static final TagKey<Item> BEE_FOOD = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "bee_food"));

    private AbyssBeeFlowers() {
    }

    public static boolean isModBlock(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace().equals(TheabyssMod.MODID);
    }

    public static boolean isModItem(ItemStack stack) {
        return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(TheabyssMod.MODID);
    }

    public static boolean attractsBees(BlockState state) {
        if (!state.is(BEE_ATTRACTIVE)) {
            return false;
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
            return false;
        }
        return !state.is(Blocks.SUNFLOWER) || state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER;
    }

    public static boolean isBeeFood(ItemStack stack) {
        return stack.is(BEE_FOOD);
    }
}
