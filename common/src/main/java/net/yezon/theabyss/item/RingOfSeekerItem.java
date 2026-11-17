package net.yezon.theabyss.item;

import net.minecraft.world.InteractionResultHolder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.yezon.theabyss.events.ApplyRingOfSeekerEvents;
import net.yezon.theabyss.util.AbyssTooltip;

public class RingOfSeekerItem extends Item {
    public RingOfSeekerItem(Item.Properties properties) {
        super(properties.durability(250).rarity(Rarity.RARE));
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> tooltipLines, TooltipFlag flag) {
        Consumer<Component> tooltip = tooltipLines::add;
        AbyssTooltip.ring(tooltip, "tooltip.theabyss.ring_of_seeker.ability", "tooltip.theabyss.ring_of_seeker.text", "tooltip.theabyss.ring_of_seeker.action", 50);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(world, entity, hand);
        BlockHitResult hit = getPlayerPOVHitResult(world, entity, ClipContext.Fluid.NONE);
        BlockPos pos = hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : entity.blockPosition();
        ApplyRingOfSeekerEvents.execute(world, pos.getX(), pos.getY(), pos.getZ(), entity, entity.getItemInHand(hand));
        return result;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ApplyRingOfSeekerEvents.execute(
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
