package net.yezon.theabyss.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

public abstract class AberytheArmorItem extends ArmorItem {
    public AberytheArmorItem(ArmorItem.Type type, Item.Properties properties) {
        super(TheabyssModArmorMaterials.ABERYTHE.holder(), type, TheabyssModArmorMaterials.ABERYTHE.properties(properties, type));
    }

    public static class Boots extends AberytheArmorItem {
        public Boots(Item.Properties properties) {
            super(ArmorItem.Type.BOOTS, properties);
        }
    }

    public static class Chestplate extends AberytheArmorItem {
        public Chestplate(Item.Properties properties) {
            super(ArmorItem.Type.CHESTPLATE, properties);
        }
    }

    public static class Helmet extends AberytheArmorItem {
        public Helmet(Item.Properties properties) {
            super(ArmorItem.Type.HELMET, properties);
        }
    }

    public static class Leggings extends AberytheArmorItem {
        public Leggings(Item.Properties properties) {
            super(ArmorItem.Type.LEGGINGS, properties);
        }
    }
}
