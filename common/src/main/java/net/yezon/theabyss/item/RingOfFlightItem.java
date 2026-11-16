package net.yezon.theabyss.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.util.AbyssTooltip;

public class RingOfFlightItem extends Item {
    public RingOfFlightItem(Item.Properties properties) {
        super(properties.durability(100).rarity(Rarity.RARE));
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public void appendHoverText(
        ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag
    ) {
        AbyssTooltip.ability(tooltip, "tooltip.theabyss.ring_of_flight.ability", "tooltip.theabyss.ring_of_flight.text");
        tooltip.accept(AbyssTooltip.text("tooltip.theabyss.ring_of_flight.note"));
        tooltip.accept(AbyssTooltip.somniumFlow(0.5, "tooltip.theabyss.per_tick"));
    }
}
