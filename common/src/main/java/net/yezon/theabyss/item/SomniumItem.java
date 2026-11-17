package net.yezon.theabyss.item;

import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.events.GetSomniumAdvEvents;
import org.jspecify.annotations.Nullable;

public class SomniumItem extends Item {
    public SomniumItem(Item.Properties properties) {
        super(properties);
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
            GetSomniumAdvEvents.execute(entity);
        }

        GetSomniumAdvEvents.execute(entity);
    }

    @Override
    public void onCraftedBy(ItemStack itemstack, Level level, Player entity) {
        super.onCraftedBy(itemstack, level, entity);
        GetSomniumAdvEvents.execute(entity);
    }
}
