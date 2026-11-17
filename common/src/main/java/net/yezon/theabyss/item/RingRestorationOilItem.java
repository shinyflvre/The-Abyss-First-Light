package net.yezon.theabyss.item;

import net.minecraft.world.InteractionResultHolder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.alchemy.RingRestoration;
import net.yezon.theabyss.util.AbyssTooltip;

public class RingRestorationOilItem extends Item {
    public RingRestorationOilItem(Item.Properties properties) {
        super(properties.stacksTo(16).rarity(Rarity.UNCOMMON));
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> tooltipLines, TooltipFlag flag) {
        Consumer<Component> tooltip = tooltipLines::add;
        AbyssTooltip.ability(tooltip, "tooltip.theabyss.ring_restoration_oil.ability", "tooltip.theabyss.ring_restoration_oil.text");
        tooltip.accept(AbyssTooltip.rightClick("tooltip.theabyss.apply_oil"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
        }
        RingRestoration.Result result = RingRestoration.restore(player, RingRestoration.POINTS);
        if (result.restored() <= 0) {
            player.displayClientMessage(Component.translatable("message.theabyss.ring_oil.none"), true);
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        player.getItemInHand(hand).consume(1, player);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 1.0F, 0.8F);
        player.displayClientMessage(Component.translatable("message.theabyss.ring_oil.done", result.restored(), result.rings()), true);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
