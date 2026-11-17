package net.yezon.theabyss.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.events.AntiInfectUseEvents;

public class AntiInfectEssenceItem extends AbyssDrinkItem {
    public AntiInfectEssenceItem(Item.Properties properties) {
        super(
            properties.stacksTo(16)
                .rarity(Rarity.RARE)
                .food(AbyssFoods.consumeSeconds(new FoodProperties.Builder().nutrition(4).saturationModifier(0.2F).alwaysEdible().build(), 1.5F)),
            1.5F
        );
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
        ItemStack retval = super.finishUsingItem(itemstack, world, entity);
        AntiInfectUseEvents.execute(entity);
        return retval;
    }
}
