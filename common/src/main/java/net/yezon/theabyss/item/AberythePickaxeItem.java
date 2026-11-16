package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class AberythePickaxeItem extends Item {
    public AberythePickaxeItem(Item.Properties properties) {
        super(
            properties.pickaxe(
                    TheabyssModToolMaterials.mining(TheabyssModToolMaterials.ABERYTHE, 4, 4.0F),
                    6.0F,
                    -2.5F
                )
        );
    }
}
