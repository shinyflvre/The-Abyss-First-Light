package net.yezon.theabyss.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

public abstract class GarniteArmorItem extends ArmorItem {
    public GarniteArmorItem(ArmorItem.Type type, Item.Properties properties) {
        super(TheabyssModArmorMaterials.GARNITE.holder(), type, TheabyssModArmorMaterials.GARNITE.properties(properties, type));
    }

    public static class Boots extends GarniteArmorItem {
        public Boots(Item.Properties properties) {
            super(ArmorItem.Type.BOOTS, properties);
        }
    }

    public static class Chestplate extends GarniteArmorItem {
        public Chestplate(Item.Properties properties) {
            super(ArmorItem.Type.CHESTPLATE, properties);
        }
    }

    public static class Helmet extends GarniteArmorItem {
        public Helmet(Item.Properties properties) {
            super(ArmorItem.Type.HELMET, properties);
        }
    }

    public static class Leggings extends GarniteArmorItem {
        public Leggings(Item.Properties properties) {
            super(ArmorItem.Type.LEGGINGS, properties);
        }
    }
}
