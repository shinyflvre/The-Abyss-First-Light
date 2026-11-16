package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class BonePickaxeItem extends Item {
    public BonePickaxeItem(Item.Properties properties) {
        super(properties.pickaxe(TheabyssModToolMaterials.mining(TheabyssModToolMaterials.BONE, 3, 3.0F), 5.0F, -2.6F));
    }
}
