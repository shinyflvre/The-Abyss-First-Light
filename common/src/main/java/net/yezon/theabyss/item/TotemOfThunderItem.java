package net.yezon.theabyss.item;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.util.AbyssTooltip;

public class TotemOfThunderItem extends Item implements AbyssTotemEffect.Totem {
    private static final AbyssTotemEffect EFFECT = AbyssTotemEffect.protection(AbyssTotemEffect.Kind.THUNDER);

    public TotemOfThunderItem(Item.Properties properties) {
        super(properties.durability(100).rarity(Rarity.EPIC));
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> tooltipLines, TooltipFlag flag) {
        Consumer<Component> tooltip = tooltipLines::add;
        AbyssTooltip.ability(tooltip, "tooltip.theabyss.totem_of_thunder.ability", "tooltip.theabyss.totem_of_thunder.text");
    }

    @Override
    public AbyssTotemEffect totemEffect() {
        return EFFECT;
    }
}
