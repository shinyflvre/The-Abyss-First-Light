package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class AberytheAxeItem extends Item {
    public AberytheAxeItem(Item.Properties properties) {
        super(properties.axe(TheabyssModToolMaterials.mining(TheabyssModToolMaterials.ABERYTHE, 3, 8.5F), 9.0F, -2.8F));
    }
}
