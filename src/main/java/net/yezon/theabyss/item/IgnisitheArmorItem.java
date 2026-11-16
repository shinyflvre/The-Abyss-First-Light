package net.yezon.theabyss.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;
import net.yezon.theabyss.events.IgnisitheArmorBodyAbilityEvents;
import org.jspecify.annotations.Nullable;

public abstract class IgnisitheArmorItem extends Item {
    public IgnisitheArmorItem(ArmorType type, Item.Properties properties) {
        super(properties.humanoidArmor(TheabyssModArmorMaterials.IGNISITHE, type));
    }

    public static class Boots extends IgnisitheArmorItem {
        public Boots(Item.Properties properties) {
            super(ArmorType.BOOTS, properties);
        }
    }

    public static class Chestplate extends IgnisitheArmorItem {
        public Chestplate(Item.Properties properties) {
            super(ArmorType.CHESTPLATE, properties);
        }

        @Override
        public void appendHoverText(
            ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag
        ) {
            tooltip.accept(Component.translatable("tooltip.theabyss.buff.fire_resistance"));
        }

        @Override
        public void inventoryTick(ItemStack itemstack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
            super.inventoryTick(itemstack, world, entity, slot);
            if (entity instanceof Player && slot != null && slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                IgnisitheArmorBodyAbilityEvents.execute(entity);
            }
        }
    }

    public static class Helmet extends IgnisitheArmorItem {
        public Helmet(Item.Properties properties) {
            super(ArmorType.HELMET, properties);
        }
    }

    public static class Leggings extends IgnisitheArmorItem {
        public Leggings(Item.Properties properties) {
            super(ArmorType.LEGGINGS, properties);
        }
    }
}
