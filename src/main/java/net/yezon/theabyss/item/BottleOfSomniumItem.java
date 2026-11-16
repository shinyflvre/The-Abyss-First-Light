package net.yezon.theabyss.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.events.BottleOfManaDrinkedEvents;

public class BottleOfSomniumItem extends Item {
    public BottleOfSomniumItem(Item.Properties properties) {
        super(
            properties.stacksTo(16)
                .rarity(Rarity.RARE)
                .food(
                    new FoodProperties.Builder().nutrition(4).saturationModifier(0.2F).alwaysEdible().build(),
                    Consumables.defaultDrink().consumeSeconds(1.5F).build()
                )
        );
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
        ItemStack retval = super.finishUsingItem(itemstack, world, entity);
        BottleOfManaDrinkedEvents.execute(entity);
        return retval;
    }
}
