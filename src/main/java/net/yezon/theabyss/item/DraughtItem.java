package net.yezon.theabyss.item;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.util.AbyssTooltip;

public class DraughtItem extends Item {
    private final String id;
    private final Consumer<LivingEntity> effect;

    public DraughtItem(Item.Properties properties, String id, Rarity rarity, Consumer<LivingEntity> effect) {
        super(properties.stacksTo(16).rarity(rarity).component(DataComponents.CONSUMABLE, Consumables.defaultDrink().consumeSeconds(1.5F).build()));
        this.id = id;
        this.effect = effect;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        AbyssTooltip.ability(tooltip, "tooltip.theabyss." + this.id + ".ability", "tooltip.theabyss." + this.id + ".text");
        tooltip.accept(AbyssTooltip.rightClick("tooltip.theabyss.drink"));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(itemstack, level, entity);
        this.effect.accept(entity);
        return result;
    }
}
