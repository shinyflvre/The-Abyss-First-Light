package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class AberytheShovelItem extends Item {
    public AberytheShovelItem(Item.Properties properties) {
        super(
            properties.shovel(
                    TheabyssModToolMaterials.mining(TheabyssModToolMaterials.ABERYTHE, 0, 8.0F),
                    4.5F,
                    -2.5F
                )
        );
    }
}
