package net.yezon.theabyss.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.events.RottenFleshRandomEffectEvents;

public class RottenFleshItem extends Item {
    public RottenFleshItem(Item.Properties properties) {
        super(properties.food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.3F).build()));
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
        ItemStack retval = super.finishUsingItem(itemstack, world, entity);
        RottenFleshRandomEffectEvents.execute(entity);
        return retval;
    }
}
