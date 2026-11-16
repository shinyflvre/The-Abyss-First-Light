package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class UnoritheHoeItem extends Item {
    public UnoritheHoeItem(Item.Properties properties) {
        super(properties.hoe(TheabyssModToolMaterials.mining(TheabyssModToolMaterials.UNORITHE, 0, 0.0F), 4.5F, 16.0F));
    }
}
