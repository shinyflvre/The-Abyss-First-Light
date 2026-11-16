package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class KnightHoeItem extends Item {
    public KnightHoeItem(Item.Properties properties) {
        super(properties.hoe(TheabyssModToolMaterials.mining(TheabyssModToolMaterials.KNIGHT, 0, 0.0F), 6.5F, -2.0F));
    }
}
