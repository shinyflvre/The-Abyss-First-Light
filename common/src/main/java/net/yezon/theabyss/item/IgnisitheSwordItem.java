package net.yezon.theabyss.item;

import net.yezon.theabyss.item.TheabyssModToolMaterials.ToolMaterial;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.events.IgnisitheSwordAbilityEvents;

public class IgnisitheSwordItem extends SwordItem {
    private static final ToolMaterial MATERIAL = TheabyssModToolMaterials.IGNISITHE;

    public IgnisitheSwordItem(Item.Properties properties) {
        super(MATERIAL, TheabyssModToolMaterials.tool(properties, MATERIAL, 7.5F, -2.0F));
    }

    @Override
    public boolean hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
        boolean retval = super.hurtEnemy(itemstack, entity, sourceentity);
        IgnisitheSwordAbilityEvents.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
        return retval;
    }
}
