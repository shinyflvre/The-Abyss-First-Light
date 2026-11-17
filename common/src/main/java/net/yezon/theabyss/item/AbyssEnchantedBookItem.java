package net.yezon.theabyss.item;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.yezon.theabyss.enchant.AbyssEnchantments;
import net.yezon.theabyss.item.hooks.EnchantmentTargetItem;
import net.yezon.theabyss.item.hooks.GameplayEnchantmentsItem;

public class AbyssEnchantedBookItem extends Item implements GameplayEnchantmentsItem, EnchantmentTargetItem {
    public AbyssEnchantedBookItem(Item.Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON).component(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
            .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
    }

    @Override
    public int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        return 0;
    }

    @Override
    public ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup) {
        return ItemEnchantments.EMPTY;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return AbyssEnchantments.ours(enchantment);
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return false;
    }
}
