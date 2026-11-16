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
import net.yezon.theabyss.events.GlacerytheArmorBootsAbilityEvents;
import net.yezon.theabyss.events.GlacerytheArmorHelmetAbilityEvents;
import org.jspecify.annotations.Nullable;

public abstract class GlacerytheArmorItem extends Item {
    public GlacerytheArmorItem(ArmorType type, Item.Properties properties) {
        super(properties.humanoidArmor(TheabyssModArmorMaterials.GLACERYTHE, type));
    }

    public static class Boots extends GlacerytheArmorItem {
        public Boots(Item.Properties properties) {
            super(ArmorType.BOOTS, properties);
        }

        @Override
        public void appendHoverText(
            ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag
        ) {
            tooltip.accept(Component.translatable("tooltip.theabyss.buff.dolphins_grace"));
        }

        @Override
        public void inventoryTick(ItemStack itemstack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
            super.inventoryTick(itemstack, world, entity, slot);
            if (entity instanceof Player && slot != null && slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                GlacerytheArmorBootsAbilityEvents.execute(entity);
            }
        }
    }

    public static class Chestplate extends GlacerytheArmorItem {
        public Chestplate(Item.Properties properties) {
            super(ArmorType.CHESTPLATE, properties);
        }
    }

    public static class Helmet extends GlacerytheArmorItem {
        public Helmet(Item.Properties properties) {
            super(ArmorType.HELMET, properties);
        }

        @Override
        public void appendHoverText(
            ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag
        ) {
            tooltip.accept(Component.translatable("tooltip.theabyss.buff.conduit_power"));
        }

        @Override
        public void inventoryTick(ItemStack itemstack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
            super.inventoryTick(itemstack, world, entity, slot);
            if (entity instanceof Player && slot != null && slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                GlacerytheArmorHelmetAbilityEvents.execute(entity);
            }
        }
    }

    public static class Leggings extends GlacerytheArmorItem {
        public Leggings(Item.Properties properties) {
            super(ArmorType.LEGGINGS, properties);
        }
    }
}
