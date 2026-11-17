package net.yezon.theabyss.item;

import net.minecraft.world.InteractionResultHolder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.entity.PhantomAttackProjectileEntity;

public class PhantomAttackItem extends Item {
    public PhantomAttackItem(Item.Properties properties) {
        super(properties.durability(100));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemstack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack itemstack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> tooltipLines, TooltipFlag flag) {
        Consumer<Component> tooltip = tooltipLines::add;
        tooltip.accept(Component.translatable("tooltip.theabyss.phantom_attack.info"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
        entity.startUsingItem(hand);
        return InteractionResultHolder.sidedSuccess(entity.getItemInHand(hand), world.isClientSide());
    }

    @Override
    public void releaseUsing(ItemStack itemstack, Level world, LivingEntity entity, int time) {
        if (!world.isClientSide() && entity instanceof ServerPlayer player) {
            PhantomAttackProjectileEntity projectile = PhantomAttackProjectileEntity.shoot(world, entity, world.getRandom());
            itemstack.hurtAndBreak(1, entity, LivingEntity.getSlotForHand(entity.getUsedItemHand()));
            if (player.getAbilities().instabuild) {
                projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            return;
        }
    }
}
