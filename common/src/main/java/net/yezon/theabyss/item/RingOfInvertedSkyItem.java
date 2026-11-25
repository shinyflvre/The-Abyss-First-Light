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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.events.ApplyRingOfInvertedSkyEvents;
import net.yezon.theabyss.ring.InvertedSky;
import net.yezon.theabyss.util.AbyssTooltip;

public class RingOfInvertedSkyItem extends Item {
    public RingOfInvertedSkyItem(Item.Properties properties) {
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
        AbyssTooltip.ring(tooltip, "tooltip.theabyss.ring_of_inverted_sky.ability", "tooltip.theabyss.ring_of_inverted_sky.text",
            "tooltip.theabyss.ring_of_inverted_sky.action", (int)InvertedSky.COST);
        tooltip.accept(AbyssTooltip.cooldown(InvertedSky.COOLDOWN));
        net.yezon.theabyss.ring.secondary.Secondary.tooltip(this, tooltip);
    }

    @Override
    public InteractionResult use(Level world, Player entity, InteractionHand hand) {
        InteractionResult result = super.use(world, entity, hand);
        ApplyRingOfInvertedSkyEvents.execute(world, entity, entity.getItemInHand(hand));
        return result;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null) {
            ApplyRingOfInvertedSkyEvents.execute(context.getLevel(), context.getPlayer(), context.getItemInHand());
        }
        return InteractionResult.SUCCESS;
    }
}
