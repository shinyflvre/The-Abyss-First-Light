package net.yezon.theabyss.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.entity.ShurikenExplosiveProjectileEntity;
import net.yezon.theabyss.init.TheabyssModItems;

public class ShurikenExplosiveItem extends Item {
    public ShurikenExplosiveItem(Item.Properties properties) {
        super(properties.durability(8).attributes(ShurikenItem.meleeAttributes(2.2)));
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
    public void appendHoverText(
        ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag
    ) {
        tooltip.accept(Component.translatable("tooltip.theabyss.shuriken_explosive.info"));
    }

    @Override
    public InteractionResult use(Level world, Player entity, InteractionHand hand) {
        entity.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onUseTick(Level world, LivingEntity entity, ItemStack itemstack, int count) {
        if (!world.isClientSide() && entity instanceof ServerPlayer player) {
            ItemStack ammo = RangedItemHelper.findAmmo(player, TheabyssModItems.SHURIKEN_EXPLOSIVE.get());
            if (player.getAbilities().instabuild || !ammo.isEmpty()) {
                ShurikenExplosiveProjectileEntity projectile = ShurikenExplosiveProjectileEntity.shoot(world, entity, world.getRandom());
                itemstack.hurtAndBreak(1, entity, entity.getUsedItemHand());
                if (player.getAbilities().instabuild) {
                    projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                } else {
                    RangedItemHelper.consumeAmmo(player, ammo);
                }
            }

            entity.releaseUsingItem();
        }
    }
}
