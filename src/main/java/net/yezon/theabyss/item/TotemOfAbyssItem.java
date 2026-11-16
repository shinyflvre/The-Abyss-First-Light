package net.yezon.theabyss.item;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.util.AbyssTooltip;

public class TotemOfAbyssItem extends Item {
    public TotemOfAbyssItem(Item.Properties properties) {
        super(properties.durability(100).rarity(Rarity.EPIC).component(DataComponents.DEATH_PROTECTION, AbyssTotemEffect.protection(AbyssTotemEffect.Kind.ABYSS)));
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public void appendHoverText(
        ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag
    ) {
        AbyssTooltip.ability(tooltip, "tooltip.theabyss.totem_of_abyss.ability", "tooltip.theabyss.totem_of_abyss.text");
    }
}
