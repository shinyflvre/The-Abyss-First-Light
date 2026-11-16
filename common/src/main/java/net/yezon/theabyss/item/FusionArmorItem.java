package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

public abstract class FusionArmorItem extends Item {
    public FusionArmorItem(ArmorType type, Item.Properties properties) {
        super(properties.humanoidArmor(TheabyssModArmorMaterials.FUSION, type));
    }

    public static class Boots extends FusionArmorItem {
        public Boots(Item.Properties properties) {
            super(ArmorType.BOOTS, properties);
        }
    }

    public static class Chestplate extends FusionArmorItem {
        public Chestplate(Item.Properties properties) {
            super(ArmorType.CHESTPLATE, properties);
        }
    }

    public static class Helmet extends FusionArmorItem {
        public Helmet(Item.Properties properties) {
            super(ArmorType.HELMET, properties);
        }
    }

    public static class Leggings extends FusionArmorItem {
        public Leggings(Item.Properties properties) {
            super(ArmorType.LEGGINGS, properties);
        }
    }
}
