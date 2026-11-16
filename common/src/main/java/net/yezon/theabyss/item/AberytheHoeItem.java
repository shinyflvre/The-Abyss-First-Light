package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class AberytheHoeItem extends Item {
    public AberytheHoeItem(Item.Properties properties) {
        super(properties.hoe(TheabyssModToolMaterials.mining(TheabyssModToolMaterials.ABERYTHE, 0, 0.0F), 0.0F, -2.5F));
    }
}
