package net.yezon.theabyss.item.hooks;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public interface GameplayEnchantmentsItem {
    int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment);

    ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup);
}
