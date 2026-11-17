package net.yezon.theabyss.item;

import net.minecraft.world.item.ArmorItem;
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
import net.minecraft.world.item.TooltipFlag;
import net.yezon.theabyss.events.PhantomArmorBodyAbilityEvents;
import org.jspecify.annotations.Nullable;

public abstract class PhantomArmorItem extends ArmorItem {
    public PhantomArmorItem(ArmorItem.Type type, Item.Properties properties) {
        super(TheabyssModArmorMaterials.PHANTOM.holder(), type, TheabyssModArmorMaterials.PHANTOM.properties(properties, type));
    }

    public static class Boots extends PhantomArmorItem {
        public Boots(Item.Properties properties) {
            super(ArmorItem.Type.BOOTS, properties);
        }
    }

    public static class Chestplate extends PhantomArmorItem {
        public Chestplate(Item.Properties properties) {
            super(ArmorItem.Type.CHESTPLATE, properties);
        }

        @Override
        public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> tooltipLines, TooltipFlag flag) {
            Consumer<Component> tooltip = tooltipLines::add;
            tooltip.accept(Component.translatable("tooltip.theabyss.buff.luck"));
        }

        @Override
        public void inventoryTick(ItemStack itemstack, Level level, Entity entity, int slotId, boolean selected) {
            super.inventoryTick(itemstack, level, entity, slotId, selected);
            if (!(level instanceof ServerLevel world)) {
                return;
            }
            EquipmentSlot slot = InventoryTickSlot.of(entity, itemstack);
            if (entity instanceof Player && slot != null && slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                PhantomArmorBodyAbilityEvents.execute(entity);
            }
        }
    }

    public static class Helmet extends PhantomArmorItem {
        public Helmet(Item.Properties properties) {
            super(ArmorItem.Type.HELMET, properties);
        }
    }

    public static class Leggings extends PhantomArmorItem {
        public Leggings(Item.Properties properties) {
            super(ArmorItem.Type.LEGGINGS, properties);
        }
    }
}
