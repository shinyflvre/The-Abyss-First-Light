package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class KnightShovelItem extends Item {
    public KnightShovelItem(Item.Properties properties) {
        super(
            properties.shovel(
                    TheabyssModToolMaterials.mining(TheabyssModToolMaterials.KNIGHT, 0, 11.0F),
                    9.0F,
                    -2.0F
                )
        );
    }
}
