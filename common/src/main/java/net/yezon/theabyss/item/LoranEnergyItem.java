package net.yezon.theabyss.item;

import net.minecraft.world.level.Level;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.yezon.theabyss.events.GetLoranEnergyAdvEvents;
import org.jspecify.annotations.Nullable;

public class LoranEnergyItem extends Item {
    public LoranEnergyItem(Item.Properties properties) {
        super(
            properties.durability(100)
                .rarity(Rarity.RARE)
                .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
        );
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> tooltipLines, TooltipFlag flag) {
        Consumer<Component> tooltip = tooltipLines::add;
        tooltip.accept(Component.translatable("tooltip.theabyss.loran_energy.info"));
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
            GetLoranEnergyAdvEvents.execute(entity);
        }

        GetLoranEnergyAdvEvents.execute(entity);
    }

    @Override
    public void onCraftedBy(ItemStack itemstack, Level level, Player entity) {
        super.onCraftedBy(itemstack, level, entity);
        GetLoranEnergyAdvEvents.execute(entity);
    }
}
