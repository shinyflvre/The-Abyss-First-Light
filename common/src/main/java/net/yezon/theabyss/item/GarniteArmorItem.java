package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

public abstract class GarniteArmorItem extends Item {
    public GarniteArmorItem(ArmorType type, Item.Properties properties) {
        super(properties.humanoidArmor(TheabyssModArmorMaterials.GARNITE, type));
    }

    public static class Boots extends GarniteArmorItem {
        public Boots(Item.Properties properties) {
            super(ArmorType.BOOTS, properties);
        }
    }

    public static class Chestplate extends GarniteArmorItem {
        public Chestplate(Item.Properties properties) {
            super(ArmorType.CHESTPLATE, properties);
        }
    }

    public static class Helmet extends GarniteArmorItem {
        public Helmet(Item.Properties properties) {
            super(ArmorType.HELMET, properties);
        }
    }

    public static class Leggings extends GarniteArmorItem {
        public Leggings(Item.Properties properties) {
            super(ArmorType.LEGGINGS, properties);
        }
    }
}
