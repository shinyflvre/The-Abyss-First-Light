package net.yezon.theabyss.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.events.CookedFishEffectEvents;

public class CookedFishItem extends Item {
    public CookedFishItem(Item.Properties properties) {
        super(
            properties.rarity(Rarity.UNCOMMON)
                .food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.3F).build())
        );
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
        ItemStack retval = super.finishUsingItem(itemstack, world, entity);
        CookedFishEffectEvents.execute(entity);
        return retval;
    }
}
