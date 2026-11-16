package net.yezon.theabyss.item.hooks;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public interface EnchantmentTargetItem {
    boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment);

    boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment);
}
