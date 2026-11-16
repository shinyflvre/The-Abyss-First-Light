package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class IncorythePickaxeItem extends Item {
    public IncorythePickaxeItem(Item.Properties properties) {
        super(
            properties.pickaxe(
                    TheabyssModToolMaterials.mining(TheabyssModToolMaterials.INCORYTHE, 6, 6.0F),
                    10.0F,
                    -2.0F
                )
        );
    }
}
