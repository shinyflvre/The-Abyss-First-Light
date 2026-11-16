package net.yezon.theabyss.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.entity.MutatedEnderpearlProjectileEntity;
import net.yezon.theabyss.init.TheabyssModItems;

public class MutatedEnderpearlItem extends Item {
    public MutatedEnderpearlItem(Item.Properties properties) {
        super(properties.durability(4).attributes(ShurikenItem.meleeAttributes(1.0)));
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemstack) {
        return ItemUseAnimation.BOW;
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
    public InteractionResult use(Level world, Player entity, InteractionHand hand) {
        entity.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean releaseUsing(ItemStack itemstack, Level world, LivingEntity entity, int time) {
        if (!world.isClientSide() && entity instanceof ServerPlayer player) {
            ItemStack ammo = RangedItemHelper.findAmmo(player, TheabyssModItems.MUTATED_ENDERPEARL.get());
            if (player.getAbilities().instabuild || !ammo.isEmpty()) {
                MutatedEnderpearlProjectileEntity projectile = MutatedEnderpearlProjectileEntity.shoot(world, entity, world.getRandom());
                itemstack.hurtAndBreak(1, entity, entity.getUsedItemHand());
                if (player.getAbilities().instabuild) {
                    projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                } else {
                    RangedItemHelper.consumeAmmo(player, ammo);
                }
                return true;
            }
        }
        return false;
    }
}
