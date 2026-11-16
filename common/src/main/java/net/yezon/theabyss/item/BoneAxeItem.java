package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class BoneAxeItem extends Item {
    public BoneAxeItem(Item.Properties properties) {
        super(properties.axe(TheabyssModToolMaterials.mining(TheabyssModToolMaterials.BONE, 3, 7.0F), 6.5F, -2.8F));
    }
}
