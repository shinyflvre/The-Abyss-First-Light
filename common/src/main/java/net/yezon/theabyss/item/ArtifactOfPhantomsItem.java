package net.yezon.theabyss.item;

import net.minecraft.world.InteractionResultHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.entity.PhantomStaffBolts;
import net.yezon.theabyss.item.hooks.ReequipAnimationItem;
import net.yezon.theabyss.ring.RingCast;

public class ArtifactOfPhantomsItem extends Item implements ReequipAnimationItem {
    private static final double SOMNIUM = 5.0;
    private static final int COOLDOWN = 40;

    public ArtifactOfPhantomsItem(Item.Properties properties) {
        super(properties.durability(500));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemstack) {
        return UseAnim.NONE;
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
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
        entity.startUsingItem(hand);
        return InteractionResultHolder.consume(entity.getItemInHand(hand));
    }

    @Override
    public void releaseUsing(ItemStack itemstack, Level world, LivingEntity entity, int time) {
        if (!world.isClientSide() && entity instanceof ServerPlayer player) {
            RingCast cast = RingCast.start(world, player, itemstack, SOMNIUM);
            if (cast == null) {
                return;
            }
            PhantomStaffBolts.fire(player);
            cast.cooldown(COOLDOWN);
            cast.charge(SOMNIUM);
            itemstack.hurtAndBreak(1, entity, LivingEntity.getSlotForHand(entity.getUsedItemHand()));
            return;
        }
    }
}
