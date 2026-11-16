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
import net.yezon.theabyss.events.IncorytheArmorAbilityEvents;
import net.yezon.theabyss.events.IncorytheArmorBodyAbilityEvents;
import org.jspecify.annotations.Nullable;

public abstract class IncorytheArmorItem extends Item {
    public IncorytheArmorItem(ArmorType type, Item.Properties properties) {
        super(properties.humanoidArmor(TheabyssModArmorMaterials.INCORYTHE, type));
    }

    public static class Boots extends IncorytheArmorItem {
        public Boots(Item.Properties properties) {
            super(ArmorType.BOOTS, properties);
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
                IncorytheArmorAbilityEvents.execute(entity);
            }
        }
    }

    public static class Chestplate extends IncorytheArmorItem {
        public Chestplate(Item.Properties properties) {
            super(ArmorType.CHESTPLATE, properties);
        }

        @Override
        public void appendHoverText(
            ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag
        ) {
            tooltip.accept(Component.translatable("tooltip.theabyss.buff.strength"));
        }

        @Override
        public void inventoryTick(ItemStack itemstack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
            super.inventoryTick(itemstack, world, entity, slot);
            if (entity instanceof Player && slot != null && slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                IncorytheArmorBodyAbilityEvents.execute(entity);
            }
        }
    }

    public static class Helmet extends IncorytheArmorItem {
        public Helmet(Item.Properties properties) {
            super(ArmorType.HELMET, properties);
        }
    }

    public static class Leggings extends IncorytheArmorItem {
        public Leggings(Item.Properties properties) {
            super(ArmorType.LEGGINGS, properties);
        }
    }
}
