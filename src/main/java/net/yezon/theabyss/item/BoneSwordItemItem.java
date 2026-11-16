package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class BoneSwordItemItem extends Item {
    public BoneSwordItemItem(Item.Properties properties) {
        super(properties.sword(TheabyssModToolMaterials.BONE, 4.0F, -2.0F));
    }
}
