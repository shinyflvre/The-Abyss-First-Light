package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class GarniteShovelItem extends Item {
    public GarniteShovelItem(Item.Properties properties) {
        super(
            properties.shovel(
                    TheabyssModToolMaterials.mining(TheabyssModToolMaterials.GARNITE, 8, 0.0F),
                    5.0F,
                    -2.4F
                )
        );
    }
}
