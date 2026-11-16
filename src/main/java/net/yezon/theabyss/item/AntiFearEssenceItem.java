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
import net.yezon.theabyss.events.AntiFearEssenceEffectEvents;
import net.yezon.theabyss.util.AbyssTooltip;

public class AntiFearEssenceItem extends Item {
    public AntiFearEssenceItem(Item.Properties properties) {
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
        AbyssTooltip.ability(tooltip, "tooltip.theabyss.anti_fear_essence.ability", "tooltip.theabyss.anti_fear_essence.text");
        tooltip.accept(AbyssTooltip.rightClick("tooltip.theabyss.drink"));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
        ItemStack retval = super.finishUsingItem(itemstack, world, entity);
        AntiFearEssenceEffectEvents.execute(entity);
        return retval;
    }
}
