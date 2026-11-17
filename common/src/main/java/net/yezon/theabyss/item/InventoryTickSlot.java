package net.yezon.theabyss.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class InventoryTickSlot {
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    private InventoryTickSlot() {
    }

    public static @Nullable EquipmentSlot of(Entity entity, ItemStack stack) {
        if (!(entity instanceof Player player)) {
            return null;
        }
        Inventory inventory = player.getInventory();
        if (inventory.getSelected() == stack) {
            return EquipmentSlot.MAINHAND;
        }
        for (EquipmentSlot slot : ARMOR) {
            if (inventory.armor.get(slot.getIndex()) == stack) {
                return slot;
            }
        }
        if (inventory.offhand.get(0) == stack) {
            return EquipmentSlot.OFFHAND;
        }
        return null;
    }
}
