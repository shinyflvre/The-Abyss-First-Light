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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.events.ApplyRingOfOceanEvents;
import net.yezon.theabyss.util.AbyssTooltip;

public class RingOfOceanItem extends Item {
    public RingOfOceanItem(Item.Properties properties) {
        super(properties.durability(100).rarity(Rarity.UNCOMMON));
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public void appendHoverText(
        ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag
    ) {
        AbyssTooltip.ring(tooltip, "tooltip.theabyss.ring_of_ocean.ability", "tooltip.theabyss.ring_of_ocean.text", "tooltip.theabyss.ring_of_ocean.action", 15);
        tooltip.accept(AbyssTooltip.cooldown(1200));
        AbyssTooltip.text(tooltip, "tooltip.theabyss.ring_of_ocean.passive_breath", net.yezon.theabyss.ring.SomniumUpgrades.Kind.USAGE,
            net.yezon.theabyss.ring.OceanPassive.BREATH_LEVEL);
        AbyssTooltip.text(tooltip, "tooltip.theabyss.ring_of_ocean.passive_swim", net.yezon.theabyss.ring.SomniumUpgrades.Kind.USAGE,
            net.yezon.theabyss.ring.OceanPassive.SWIM_LEVEL);
        net.yezon.theabyss.ring.secondary.Secondary.tooltip(this, tooltip);
    }

    @Override
    public InteractionResult use(Level world, Player entity, InteractionHand hand) {
        InteractionResult result = super.use(world, entity, hand);
        ApplyRingOfOceanEvents.execute(world, entity.getX(), entity.getY(), entity.getZ(), entity, entity.getItemInHand(hand));
        return result;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ApplyRingOfOceanEvents.execute(
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
