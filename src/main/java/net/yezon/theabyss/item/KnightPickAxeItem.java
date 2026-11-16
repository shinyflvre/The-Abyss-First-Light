package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class KnightPickAxeItem extends Item {
    public KnightPickAxeItem(Item.Properties properties) {
        super(
            properties.pickaxe(
                    TheabyssModToolMaterials.mining(TheabyssModToolMaterials.KNIGHT, 6, 6.0F),
                    10.5F,
                    -2.0F
                )
        );
    }
}
