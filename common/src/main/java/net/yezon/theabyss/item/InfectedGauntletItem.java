package net.yezon.theabyss.item;

import net.minecraft.world.InteractionResultHolder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.ring.Gauntlet;
import net.yezon.theabyss.ring.RingMagic;

public class InfectedGauntletItem extends Item {
    public InfectedGauntletItem(Item.Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gauntlet = player.getItemInHand(hand);
        ItemStack ring = Gauntlet.selectedRing(gauntlet);
        if (ring.isEmpty() || player.getCooldowns().isOnCooldown(ring.getItem())) {
            if (!level.isClientSide()) {
                RingMagic.castFromGauntlet(player, hand, gauntlet, false);
            }
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        RingMagic.castFromGauntlet(player, hand, gauntlet, false);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipLines, TooltipFlag flag) {
        Consumer<Component> tooltip = tooltipLines::add;
        var rings = Gauntlet.rings(stack);
        int selected = Gauntlet.selected(stack);
        for (int i = 0; i < Gauntlet.SLOTS; i++) {
            ItemStack ring = rings.get(i);
            Component name = ring.isEmpty() ? Component.translatable("gauntlet.theabyss.slot_empty").withStyle(ChatFormatting.DARK_GRAY)
                : ring.getHoverName().copy().withStyle(i == selected ? ChatFormatting.AQUA : ChatFormatting.GRAY);
            tooltip.accept(Component.literal(i == selected ? "▶ " : "  ").withStyle(ChatFormatting.AQUA).append(name));
        }
        tooltip.accept(Component.translatable("gauntlet.theabyss.hint").withStyle(ChatFormatting.DARK_AQUA));
    }
}
