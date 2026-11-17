package net.yezon.theabyss.item;

import net.minecraft.world.level.Level;
import java.util.List;
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
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.events.AmuledOfNosajEffectEvents;
import org.jspecify.annotations.Nullable;
import net.yezon.theabyss.util.AbyssTooltip;

public class AmuledOfNosajItem extends Item {
    public AmuledOfNosajItem(Item.Properties properties) {
        super(properties.durability(100).rarity(Rarity.EPIC));
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> tooltipLines, TooltipFlag flag) {
        Consumer<Component> tooltip = tooltipLines::add;
        AbyssTooltip.ability(tooltip, "tooltip.theabyss.amuled_of_nosaj.ability", "tooltip.theabyss.amuled_of_nosaj.text");
        tooltip.accept(AbyssTooltip.text("tooltip.theabyss.works_from_inventory"));
    }

    @Override
    public void inventoryTick(ItemStack itemstack, Level level, Entity entity, int slotId, boolean selected) {
        super.inventoryTick(itemstack, level, entity, slotId, selected);
        if (!(level instanceof ServerLevel world)) {
            return;
        }
        EquipmentSlot slot = InventoryTickSlot.of(entity, itemstack);
        if (!(entity instanceof Player)) {
            return;
        }
        if (slot == EquipmentSlot.MAINHAND) {
            AmuledOfNosajEffectEvents.execute(entity);
        }

        AmuledOfNosajEffectEvents.execute(entity);
    }
}
