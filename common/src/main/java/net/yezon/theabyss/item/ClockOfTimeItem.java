package net.yezon.theabyss.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.events.ApplyClockOfTimeEvents;
import net.yezon.theabyss.util.AbyssTooltip;

public class ClockOfTimeItem extends Item {
    public ClockOfTimeItem(Item.Properties properties) {
        super(properties.durability(100).rarity(Rarity.EPIC));
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public void appendHoverText(
        ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag
    ) {
        AbyssTooltip.ability(tooltip, "tooltip.theabyss.clock_of_time.ability", "tooltip.theabyss.clock_of_time.text");
        tooltip.accept(AbyssTooltip.rightClick("tooltip.theabyss.clock_of_time.action"));
        tooltip.accept(AbyssTooltip.somnium(50));
    }

    @Override
    public InteractionResult use(Level world, Player entity, InteractionHand hand) {
        InteractionResult result = super.use(world, entity, hand);
        ApplyClockOfTimeEvents.execute(world, entity, entity.getItemInHand(hand));
        return result;
    }
}
