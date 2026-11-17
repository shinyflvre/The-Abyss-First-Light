package net.yezon.theabyss.block;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public interface HoverTextBlock {
    void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag);
}
