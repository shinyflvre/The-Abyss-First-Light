package net.yezon.theabyss.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.timestop.TimeStop;
import net.yezon.theabyss.util.AbyssTooltip;

public class RingOfTimeItem extends Item {
    public RingOfTimeItem(Item.Properties properties) {
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
        AbyssTooltip.ring(tooltip, "tooltip.theabyss.ring_of_time.ability", "tooltip.theabyss.ring_of_time.text", "tooltip.theabyss.ring_of_time.action", 80);
        tooltip.accept(AbyssTooltip.cooldown(net.yezon.theabyss.timestop.TimeStop.RING_COOLDOWN));
        net.yezon.theabyss.ring.secondary.Secondary.tooltip(this, tooltip);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return cast(player, player.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return context.getPlayer() != null ? cast(context.getPlayer(), context.getItemInHand()) : InteractionResult.PASS;
    }

    private static InteractionResult cast(Player player, ItemStack ring) {
        if (player instanceof ServerPlayer caster) {
            return TimeStop.useRing(caster, ring) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }
}
