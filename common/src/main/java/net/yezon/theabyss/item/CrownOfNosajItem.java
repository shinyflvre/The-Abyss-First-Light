package net.yezon.theabyss.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.events.CrownOfNosajEffectEvents;
import org.jspecify.annotations.Nullable;
import net.yezon.theabyss.util.AbyssTooltip;

public class CrownOfNosajItem extends Item {
    public CrownOfNosajItem(Item.Properties properties) {
        super(properties.durability(100).rarity(Rarity.EPIC));
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public void appendHoverText(
        ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag
    ) {
        AbyssTooltip.ability(tooltip, "tooltip.theabyss.crown_of_nosaj.ability", "tooltip.theabyss.crown_of_nosaj.text");
        tooltip.accept(AbyssTooltip.text("tooltip.theabyss.works_from_inventory"));
    }

    @Override
    public void inventoryTick(ItemStack itemstack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(itemstack, world, entity, slot);
        if (!(entity instanceof Player)) {
            return;
        }
        if (slot == EquipmentSlot.MAINHAND) {
            CrownOfNosajEffectEvents.execute(entity);
        }

        CrownOfNosajEffectEvents.execute(entity);
    }
}
