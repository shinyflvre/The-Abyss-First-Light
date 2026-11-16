package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class UnorithePickAxeItem extends Item {
    public UnorithePickAxeItem(Item.Properties properties) {
        super(
            properties.pickaxe(
                    TheabyssModToolMaterials.mining(TheabyssModToolMaterials.UNORITHE, 5, 5.0F),
                    8.5F,
                    -2.3F
                )
        );
    }
}
