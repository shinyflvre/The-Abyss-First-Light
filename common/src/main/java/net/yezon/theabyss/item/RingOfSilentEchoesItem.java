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
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.ring.SomniumUpgrades;
import net.yezon.theabyss.ring.echo.EchoVision;
import net.yezon.theabyss.ring.echo.SilentEchoes;
import net.yezon.theabyss.ring.secondary.Secondary;
import net.yezon.theabyss.util.AbyssTooltip;

public class RingOfSilentEchoesItem extends Item {
    public RingOfSilentEchoesItem(Item.Properties properties) {
        super(properties.durability(100).rarity(Rarity.RARE));
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> tooltipLines, TooltipFlag flag) {
        Consumer<Component> tooltip = tooltipLines::add;
        AbyssTooltip.ability(tooltip, "tooltip.theabyss.ring_of_silent_echoes.ability", "tooltip.theabyss.ring_of_silent_echoes.text");
        tooltip.accept(AbyssTooltip.rightClick("tooltip.theabyss.ring_of_silent_echoes.action"));
        tooltip.accept(AbyssTooltip.somnium(SilentEchoes.COST));
        tooltip.accept(AbyssTooltip.cooldown(SilentEchoes.COOLDOWN));
        Secondary.tooltip(this, tooltip);
        int range = (int)EchoVision.range(AbyssTooltip.level(SomniumUpgrades.Kind.USAGE));
        tooltip.accept(AbyssTooltip.text(AbyssTooltip.tr("tooltip.theabyss.ring_of_silent_echoes.secondary.range", range)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            SilentEchoes.cast(level, player, player.getItemInHand(hand));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
