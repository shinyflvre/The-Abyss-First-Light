package net.yezon.theabyss.item;

import net.minecraft.world.InteractionResultHolder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.events.ApplyRingOfPocketRoomEvents;
import net.yezon.theabyss.util.AbyssTooltip;

public class RingOfPocketBoxItem extends Item {
    public RingOfPocketBoxItem(Item.Properties properties) {
        super(properties.durability(100).rarity(Rarity.RARE));
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> tooltipLines, TooltipFlag flag) {
        Consumer<Component> tooltip = tooltipLines::add;
        AbyssTooltip.ring(tooltip, "tooltip.theabyss.ring_of_pocket_box.ability", "tooltip.theabyss.ring_of_pocket_box.text", "tooltip.theabyss.ring_of_pocket_box.action", 50);
        tooltip.accept(AbyssTooltip.cooldown(500));
        net.yezon.theabyss.ring.secondary.Secondary.tooltip(this, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(world, entity, hand);
        ApplyRingOfPocketRoomEvents.execute(world, entity.getX(), entity.getY(), entity.getZ(), entity, entity.getItemInHand(hand));
        return result;
    }
}
