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
import net.yezon.theabyss.events.ApplyRingOfCrystalsEvents;
import net.yezon.theabyss.util.AbyssTooltip;

public class RingOfCrystalsItem extends Item {
    public RingOfCrystalsItem(Item.Properties properties) {
        super(properties.durability(100).rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> tooltipLines, TooltipFlag flag) {
        Consumer<Component> tooltip = tooltipLines::add;
        AbyssTooltip.ring(tooltip, "tooltip.theabyss.ring_of_crystals.ability", "tooltip.theabyss.ring_of_crystals.text",
            "tooltip.theabyss.ring_of_crystals.action", ApplyRingOfCrystalsEvents.SLASH_COST);
        tooltip.accept(AbyssTooltip.damage(ApplyRingOfCrystalsEvents.slashDamage(AbyssTooltip.damageUpgrade()), ApplyRingOfCrystalsEvents.slashDamage(0), ""));
        tooltip.accept(AbyssTooltip.cooldown(ApplyRingOfCrystalsEvents.SLASH_COOLDOWN));
        Consumer<Component> needles = AbyssTooltip.ability(tooltip, "tooltip.theabyss.ring_of_crystals.needles.ability", "tooltip.theabyss.ring_of_crystals.needles.text",
            ApplyRingOfCrystalsEvents.NEEDLES_LEVEL);
        needles.accept(AbyssTooltip.middleClick("tooltip.theabyss.ring_of_crystals.needles.action"));
        needles.accept(AbyssTooltip.somnium(ApplyRingOfCrystalsEvents.NEEDLES_COST));
        needles.accept(AbyssTooltip.damage(ApplyRingOfCrystalsEvents.needleDamage(AbyssTooltip.damageUpgrade()), ApplyRingOfCrystalsEvents.needleDamage(0), "tooltip.theabyss.times_5"));
        needles.accept(AbyssTooltip.cooldown(ApplyRingOfCrystalsEvents.NEEDLES_COOLDOWN));
        tooltip.accept(AbyssTooltip.text("tooltip.theabyss.ring_of_crystals.note"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ApplyRingOfCrystalsEvents.slash(level, player, player.getItemInHand(hand));
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
