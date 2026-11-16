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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.astral.AstralPlane;
import net.yezon.theabyss.util.AbyssTooltip;

public class RingOfSpectralItem extends Item {
    public RingOfSpectralItem(Item.Properties properties) {
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
        AbyssTooltip.ability(tooltip, "tooltip.theabyss.ring_of_spectral.ability", "tooltip.theabyss.ring_of_spectral.text");
        tooltip.accept(AbyssTooltip.text("tooltip.theabyss.ring_of_spectral.note"));
        tooltip.accept(AbyssTooltip.rightClick("tooltip.theabyss.ring_of_spectral.action"));
        tooltip.accept(AbyssTooltip.somnium(15, 1, "tooltip.theabyss.per_second"));
        net.yezon.theabyss.ring.secondary.Secondary.tooltip(this, tooltip);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            boolean entering = !AstralPlane.isAstral(serverPlayer);
            if (AstralPlane.toggle(serverPlayer)) {
                ItemStack stack = player.getItemInHand(hand);
                player.getCooldowns().addCooldown(stack, 20);
                if (entering) {
                    net.yezon.theabyss.ring.RingWear.wear(stack, serverPlayer.level(), serverPlayer, 1);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
