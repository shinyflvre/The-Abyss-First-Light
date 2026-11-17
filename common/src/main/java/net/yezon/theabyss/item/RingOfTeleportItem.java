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
import net.yezon.theabyss.events.ApplyRingOfTeleportEvents;
import net.yezon.theabyss.util.AbyssTooltip;

public class RingOfTeleportItem extends Item {
    public RingOfTeleportItem(Item.Properties properties) {
        super(properties.durability(100).rarity(Rarity.RARE));
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> tooltipLines, TooltipFlag flag) {
        Consumer<Component> tooltip = tooltipLines::add;
        AbyssTooltip.ability(tooltip, "tooltip.theabyss.ring_of_teleport.ability", AbyssTooltip.tr("tooltip.theabyss.ring_of_teleport.text", net.yezon.theabyss.events.ApplyRingOfTeleportEvents.distance(AbyssTooltip.ringLevel())));
        tooltip.accept(AbyssTooltip.rightClick("tooltip.theabyss.ring_of_teleport.ability"));
        tooltip.accept(AbyssTooltip.somnium(20));
        tooltip.accept(AbyssTooltip.cooldown(20));
        net.yezon.theabyss.ring.secondary.Secondary.tooltip(this, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(world, entity, hand);
        ApplyRingOfTeleportEvents.execute(world, entity.getX(), entity.getY(), entity.getZ(), entity, entity.getItemInHand(hand));
        return result;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ApplyRingOfTeleportEvents.execute(
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
