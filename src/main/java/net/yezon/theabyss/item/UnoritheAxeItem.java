package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class UnoritheAxeItem extends Item {
    public UnoritheAxeItem(Item.Properties properties) {
        super(
            properties.axe(
                    TheabyssModToolMaterials.mining(TheabyssModToolMaterials.UNORITHE, 5, 9.5F),
                    12.0F,
                    -2.5F
                )
        );
    }
}
