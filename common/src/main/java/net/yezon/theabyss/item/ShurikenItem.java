package net.yezon.theabyss.item;

import net.minecraft.world.InteractionResultHolder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.entity.ShurikenProjectileEntity;
import net.yezon.theabyss.init.TheabyssModItems;

public class ShurikenItem extends Item {
    public ShurikenItem(Item.Properties properties) {
        super(properties.durability(32).attributes(meleeAttributes(2.2)));
    }

    static ItemAttributeModifiers meleeAttributes(double attackDamage) {
        return ItemAttributeModifiers.builder()
            .add(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
            )
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build();
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
        tooltip.accept(Component.translatable("tooltip.theabyss.shuriken.info"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
        entity.startUsingItem(hand);
        return InteractionResultHolder.sidedSuccess(entity.getItemInHand(hand), world.isClientSide());
    }

    @Override
    public void onUseTick(Level world, LivingEntity entity, ItemStack itemstack, int count) {
        if (!world.isClientSide() && entity instanceof ServerPlayer player) {
            ItemStack ammo = RangedItemHelper.findAmmo(player, TheabyssModItems.SHURIKEN.get());
            if (player.getAbilities().instabuild || !ammo.isEmpty()) {
                ShurikenProjectileEntity projectile = ShurikenProjectileEntity.shoot(world, entity, world.getRandom());
                itemstack.hurtAndBreak(1, entity, LivingEntity.getSlotForHand(entity.getUsedItemHand()));
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
