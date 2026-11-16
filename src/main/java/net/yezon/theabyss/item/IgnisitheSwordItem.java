package net.yezon.theabyss.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.events.IgnisitheSwordAbilityEvents;

public class IgnisitheSwordItem extends Item {
    public IgnisitheSwordItem(Item.Properties properties) {
        super(properties.sword(TheabyssModToolMaterials.IGNISITHE, 7.5F, -2.0F));
    }

    @Override
    public void hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
        super.hurtEnemy(itemstack, entity, sourceentity);
        IgnisitheSwordAbilityEvents.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
    }
}
