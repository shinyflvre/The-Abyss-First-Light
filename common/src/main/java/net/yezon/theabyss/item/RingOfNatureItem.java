package net.yezon.theabyss.item;

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
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.yezon.theabyss.events.ApplyRingOfNatureEvents;
import net.yezon.theabyss.util.AbyssTooltip;

public class RingOfNatureItem extends Item {
    public RingOfNatureItem(Item.Properties properties) {
        super(properties.durability(250).rarity(Rarity.UNCOMMON));
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public void appendHoverText(
        ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag
    ) {
        AbyssTooltip.ring(tooltip, "tooltip.theabyss.ring_of_nature.ability", "tooltip.theabyss.ring_of_nature.text", "tooltip.theabyss.ring_of_nature.action", 8);
    }

    @Override
    public InteractionResult use(Level world, Player entity, InteractionHand hand) {
        InteractionResult result = super.use(world, entity, hand);
        BlockHitResult hit = getPlayerPOVHitResult(world, entity, ClipContext.Fluid.NONE);
        BlockPos pos = hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : entity.blockPosition().below();
        ApplyRingOfNatureEvents.execute(world, pos.getX(), pos.getY(), pos.getZ(), entity, entity.getItemInHand(hand));
        return result;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ApplyRingOfNatureEvents.execute(
            context.getLevel(),
            context.getClickedPos().getX(),
            context.getClickedPos().getY(),
            context.getClickedPos().getZ(),
            context.getPlayer(),
            context.getItemInHand()
        );
        return InteractionResult.SUCCESS;
    }
}
