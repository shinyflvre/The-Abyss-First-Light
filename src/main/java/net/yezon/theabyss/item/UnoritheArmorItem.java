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
import net.yezon.theabyss.events.UnoritheArmorBootsAbilityEvents;
import net.yezon.theabyss.events.UnoritheArmorLeggingsAbilityEvents;
import org.jspecify.annotations.Nullable;

public abstract class UnoritheArmorItem extends Item {
    public UnoritheArmorItem(ArmorType type, Item.Properties properties) {
        super(properties.humanoidArmor(TheabyssModArmorMaterials.UNORITHE, type));
    }

    public static class Boots extends UnoritheArmorItem {
        public Boots(Item.Properties properties) {
            super(ArmorType.BOOTS, properties);
        }

        @Override
        public void appendHoverText(
            ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag
        ) {
            tooltip.accept(Component.translatable("tooltip.theabyss.buff.jump_boost"));
        }

        @Override
        public void inventoryTick(ItemStack itemstack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
            super.inventoryTick(itemstack, world, entity, slot);
            if (entity instanceof Player && slot != null && slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                UnoritheArmorBootsAbilityEvents.execute(entity);
            }
        }
    }

    public static class Chestplate extends UnoritheArmorItem {
        public Chestplate(Item.Properties properties) {
            super(ArmorType.CHESTPLATE, properties);
        }
    }

    public static class Helmet extends UnoritheArmorItem {
        public Helmet(Item.Properties properties) {
            super(ArmorType.HELMET, properties);
        }
    }

    public static class Leggings extends UnoritheArmorItem {
        public Leggings(Item.Properties properties) {
            super(ArmorType.LEGGINGS, properties);
        }

        @Override
        public void appendHoverText(
            ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag
        ) {
            tooltip.accept(Component.translatable("tooltip.theabyss.buff.speed"));
        }

        @Override
        public void inventoryTick(ItemStack itemstack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
            super.inventoryTick(itemstack, world, entity, slot);
            if (entity instanceof Player && slot != null && slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                UnoritheArmorLeggingsAbilityEvents.execute(entity);
            }
        }
    }
}
