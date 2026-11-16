package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class IncorytheShovelItem extends Item {
    public IncorytheShovelItem(Item.Properties properties) {
        super(
            properties.shovel(
                    TheabyssModToolMaterials.mining(TheabyssModToolMaterials.INCORYTHE, 0, 10.5F),
                    8.5F,
                    -2.0F
                )
        );
    }
}
