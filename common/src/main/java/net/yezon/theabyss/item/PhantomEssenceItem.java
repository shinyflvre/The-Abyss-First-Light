package net.yezon.theabyss.item;

import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.yezon.theabyss.events.PhantomSoulItemItemInInventoryTickEvents;
import org.jspecify.annotations.Nullable;

public class PhantomEssenceItem extends Item {
    public PhantomEssenceItem(Item.Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }

    @Override
    public void inventoryTick(ItemStack itemstack, Level level, Entity entity, int slotId, boolean selected) {
        super.inventoryTick(itemstack, level, entity, slotId, selected);
        if (!(level instanceof ServerLevel world)) {
            return;
        }
        if (!(entity instanceof Player)) {
            return;
        }
        PhantomSoulItemItemInInventoryTickEvents.execute(entity);
    }
}
