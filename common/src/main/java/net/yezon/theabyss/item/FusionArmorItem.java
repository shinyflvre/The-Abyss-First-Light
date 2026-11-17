package net.yezon.theabyss.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

public abstract class FusionArmorItem extends ArmorItem {
    public FusionArmorItem(ArmorItem.Type type, Item.Properties properties) {
        super(TheabyssModArmorMaterials.FUSION.holder(), type, TheabyssModArmorMaterials.FUSION.properties(properties, type));
    }

    public static class Boots extends FusionArmorItem {
        public Boots(Item.Properties properties) {
            super(ArmorItem.Type.BOOTS, properties);
        }
    }

    public static class Chestplate extends FusionArmorItem {
        public Chestplate(Item.Properties properties) {
            super(ArmorItem.Type.CHESTPLATE, properties);
        }
    }

    public static class Helmet extends FusionArmorItem {
        public Helmet(Item.Properties properties) {
            super(ArmorItem.Type.HELMET, properties);
        }
    }

    public static class Leggings extends FusionArmorItem {
        public Leggings(Item.Properties properties) {
            super(ArmorItem.Type.LEGGINGS, properties);
        }
    }
}
