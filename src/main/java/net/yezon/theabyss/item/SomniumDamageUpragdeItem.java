package net.yezon.theabyss.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.events.ManaDurabilityUpgradeResultEvents;

public class SomniumDamageUpragdeItem extends Item {
    public SomniumDamageUpragdeItem(Item.Properties properties) {
        super(
            properties.stacksTo(16)
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
    public void appendHoverText(
        ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag
    ) {
        tooltip.accept(Component.translatable("tooltip.theabyss.somnium_damage_upgrade.1"));
        tooltip.accept(Component.translatable("tooltip.theabyss.somnium_damage_upgrade.2"));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
        ItemStack retval = super.finishUsingItem(itemstack, world, entity);
        ManaDurabilityUpgradeResultEvents.execute(world, entity.getX(), entity.getY(), entity.getZ(), entity);
        return retval;
    }
}
