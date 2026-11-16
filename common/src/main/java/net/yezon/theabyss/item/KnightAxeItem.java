package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class KnightAxeItem extends Item {
    public KnightAxeItem(Item.Properties properties) {
        super(
            properties.axe(
                    TheabyssModToolMaterials.mining(TheabyssModToolMaterials.KNIGHT, 10, 10.0F),
                    17.0F,
                    -2.5F
                )
        );
    }
}
