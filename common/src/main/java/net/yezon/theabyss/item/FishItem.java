package net.yezon.theabyss.item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.state.BlockState;

public class FishItem extends Item {
    public FishItem(Item.Properties properties) {
        super(
            properties.rarity(Rarity.UNCOMMON)
                .food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.3F).build())
        );
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }
}
