package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class IncorytheAxeItem extends Item {
    public IncorytheAxeItem(Item.Properties properties) {
        super(
            properties.axe(
                    TheabyssModToolMaterials.mining(TheabyssModToolMaterials.INCORYTHE, 7, 11.0F),
                    15.0F,
                    -2.3F
                )
        );
    }
}
