package net.yezon.theabyss.item;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

public class UnactiveFusionIngotItem extends Item {
    public UnactiveFusionIngotItem(Item.Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> tooltipLines, TooltipFlag flag) {
        Consumer<Component> tooltip = tooltipLines::add;
        tooltip.accept(Component.translatable("tooltip.theabyss.craft_armor_1"));
        tooltip.accept(Component.translatable("tooltip.theabyss.craft_armor_2"));
    }
}
