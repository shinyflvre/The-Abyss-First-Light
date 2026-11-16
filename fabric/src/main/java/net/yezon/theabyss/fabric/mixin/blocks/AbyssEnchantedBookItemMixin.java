package net.yezon.theabyss.fabric.mixin.blocks;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.yezon.theabyss.item.AbyssEnchantedBookItem;
import net.yezon.theabyss.item.hooks.EnchantmentTargetItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbyssEnchantedBookItem.class)
public abstract class AbyssEnchantedBookItemMixin implements FabricItem {
    @Override
    public boolean canBeEnchantedWith(ItemStack stack, Holder<Enchantment> enchantment, EnchantingContext context) {
        EnchantmentTargetItem item = (EnchantmentTargetItem)this;
        return context == EnchantingContext.PRIMARY ? item.isPrimaryItemFor(stack, enchantment) : item.supportsEnchantment(stack, enchantment);
    }
}
