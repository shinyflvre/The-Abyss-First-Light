package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class FusionShovelItem extends Item {
    public FusionShovelItem(Item.Properties properties) {
        super(
            properties.shovel(
                    TheabyssModToolMaterials.mining(TheabyssModToolMaterials.FUSION, 0, 8.5F),
                    6.0F,
                    -2.25F
                )
        );
    }
}
