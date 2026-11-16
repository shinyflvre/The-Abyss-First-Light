package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class UnoritheShovelItem extends Item {
    public UnoritheShovelItem(Item.Properties properties) {
        super(
            properties.shovel(
                    TheabyssModToolMaterials.mining(TheabyssModToolMaterials.UNORITHE, 0, 10.0F),
                    7.5F,
                    -2.0F
                )
        );
    }
}
