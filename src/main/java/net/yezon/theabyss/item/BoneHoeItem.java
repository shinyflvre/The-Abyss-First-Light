package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class BoneHoeItem extends Item {
    public BoneHoeItem(Item.Properties properties) {
        super(properties.hoe(TheabyssModToolMaterials.mining(TheabyssModToolMaterials.BONE, 0, 0.0F), -1.0F, -3.0F));
    }
}
