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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.events.ApplyRingOfGhostEvents;
import net.yezon.theabyss.util.AbyssTooltip;

public class RingOfGhostsItem extends Item {
    public RingOfGhostsItem(Item.Properties properties) {
        super(properties.durability(100).rarity(Rarity.EPIC));
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> tooltipLines, TooltipFlag flag) {
        Consumer<Component> tooltip = tooltipLines::add;
        AbyssTooltip.ability(tooltip, "tooltip.theabyss.ring_of_ghosts.ability", "tooltip.theabyss.ring_of_ghosts.text");
        tooltip.accept(AbyssTooltip.rightClick("tooltip.theabyss.ring_of_ghosts.action"));
        tooltip.accept(AbyssTooltip.key("X", "tooltip.theabyss.ring_of_ghosts.key_x"));
        tooltip.accept(AbyssTooltip.key("C", "tooltip.theabyss.ring_of_ghosts.key_c"));
        tooltip.accept(AbyssTooltip.key("V", "tooltip.theabyss.ring_of_ghosts.key_v"));
        tooltip.accept(AbyssTooltip.somnium(95));
        tooltip.accept(AbyssTooltip.cooldown(10000));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(world, entity, hand);
        ApplyRingOfGhostEvents.execute(world, entity.getX(), entity.getY(), entity.getZ(), entity, entity.getItemInHand(hand));
        return result;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ApplyRingOfGhostEvents.execute(
            context.getLevel(),
            context.getClickedPos().getX(),
            context.getClickedPos().getY(),
            context.getClickedPos().getZ(),
            context.getPlayer(),
            context.getItemInHand()
        );
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
