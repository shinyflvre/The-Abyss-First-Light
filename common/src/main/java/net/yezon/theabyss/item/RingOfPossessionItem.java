package net.yezon.theabyss.item;

import net.minecraft.world.InteractionResultHolder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.possession.PossessionManager;
import net.yezon.theabyss.util.AbyssTooltip;

public class RingOfPossessionItem extends Item {
    public RingOfPossessionItem(Item.Properties properties) {
        super(properties.durability(100).rarity(Rarity.RARE));
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> tooltipLines, TooltipFlag flag) {
        Consumer<Component> tooltip = tooltipLines::add;
        AbyssTooltip.ability(tooltip, "tooltip.theabyss.ring_of_possession.ability", "tooltip.theabyss.ring_of_possession.text");
        tooltip.accept(AbyssTooltip.rightClick("tooltip.theabyss.ring_of_possession.action"));
        tooltip.accept(AbyssTooltip.text("tooltip.theabyss.ring_of_possession.note"));
        tooltip.accept(AbyssTooltip.somnium(20, 2, "tooltip.theabyss.per_second"));
        net.yezon.theabyss.ring.secondary.Secondary.tooltip(this, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return new InteractionResultHolder<>(this.activate(player, hand, null), player.getItemInHand(hand));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        return this.activate(player, hand, target);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return context.getPlayer() != null ? this.activate(context.getPlayer(), context.getHand(), null) : InteractionResult.PASS;
    }

    private InteractionResult activate(Player player, InteractionHand hand, LivingEntity clicked) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (PossessionManager.isPossessing(player)) {
            int cooldown = PossessionManager.useAbility(serverPlayer);
            if (cooldown > 0) {
                player.getCooldowns().addCooldown(stack.getItem(), cooldown);
            }
            return InteractionResult.CONSUME;
        }
        Mob target = clicked instanceof Mob mob ? mob : PossessionManager.findTarget(player);
        if (target == null) {
            serverPlayer.displayClientMessage(Component.translatable("possession.theabyss.no_target"), true);
            return InteractionResult.FAIL;
        }
        if (PossessionManager.tryPossess(serverPlayer, target)) {
            net.yezon.theabyss.ring.RingWear.wear(stack, serverPlayer.serverLevel(), serverPlayer, 1);
        }
        return InteractionResult.CONSUME;
    }
}
