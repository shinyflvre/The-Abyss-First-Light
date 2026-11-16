package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class IncorytheHoeItem extends Item {
    public IncorytheHoeItem(Item.Properties properties) {
        super(
            properties.hoe(
                    TheabyssModToolMaterials.mining(TheabyssModToolMaterials.INCORYTHE, 0, 0.0F),
                    6.0F,
                    -2.0F
                )
        );
    }
}
