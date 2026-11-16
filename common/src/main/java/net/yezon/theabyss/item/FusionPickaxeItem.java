package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class FusionPickaxeItem extends Item {
    public FusionPickaxeItem(Item.Properties properties) {
        super(
            properties.pickaxe(
                    TheabyssModToolMaterials.mining(TheabyssModToolMaterials.FUSION, 4, 5.0F),
                    7.0F,
                    -2.4F
                )
        );
    }
}
