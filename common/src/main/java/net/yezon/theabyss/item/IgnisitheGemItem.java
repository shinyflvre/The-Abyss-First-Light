package net.yezon.theabyss.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class IgnisitheGemItem extends Item {
    public IgnisitheGemItem(Item.Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON));
    }

    @Override
    public void appendHoverText(
        ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag
    ) {
        tooltip.accept(Component.translatable("tooltip.theabyss.craft_armor_1"));
        tooltip.accept(Component.translatable("tooltip.theabyss.craft_armor_2"));
    }
}
