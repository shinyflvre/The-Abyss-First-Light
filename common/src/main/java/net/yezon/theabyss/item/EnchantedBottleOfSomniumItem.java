package net.yezon.theabyss.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.events.EnchantedBottleOfManaDrinkedEvents;

public class EnchantedBottleOfSomniumItem extends AbyssDrinkItem {
    public EnchantedBottleOfSomniumItem(Item.Properties properties) {
        super(
            properties.stacksTo(16)
                .rarity(Rarity.EPIC)
                .food(AbyssFoods.consumeSeconds(new FoodProperties.Builder().nutrition(4).saturationModifier(0.2F).alwaysEdible().build(), 1.5F))
                .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true),
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
        EnchantedBottleOfManaDrinkedEvents.execute(entity);
        return retval;
    }
}
