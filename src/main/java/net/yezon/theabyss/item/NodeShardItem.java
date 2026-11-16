package net.yezon.theabyss.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.events.ApplyNodeEffectEvents;

public class NodeShardItem extends Item {
    public NodeShardItem(Item.Properties properties) {
        super(
            properties.stacksTo(8)
                .rarity(Rarity.EPIC)
                .food(
                    new FoodProperties.Builder().nutrition(4).saturationModifier(0.0F).alwaysEdible().build(),
                    Consumables.defaultFood().consumeSeconds(0.05F).build()
                )
                .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
        );
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
        ItemStack retval = super.finishUsingItem(itemstack, world, entity);
        ApplyNodeEffectEvents.execute(entity);
        return retval;
    }
}
