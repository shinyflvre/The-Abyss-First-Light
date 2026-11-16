package net.yezon.theabyss.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.yezon.theabyss.events.PhantomSoulItemItemInInventoryTickEvents;
import org.jspecify.annotations.Nullable;

public class FixedBoneItem extends Item {
    public FixedBoneItem(Item.Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON));
    }

    @Override
    public void inventoryTick(ItemStack itemstack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(itemstack, world, entity, slot);
        if (!(entity instanceof Player)) {
            return;
        }
        PhantomSoulItemItemInInventoryTickEvents.execute(entity);
    }
}
