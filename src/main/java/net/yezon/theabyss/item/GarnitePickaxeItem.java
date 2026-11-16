package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class GarnitePickaxeItem extends Item {
    public GarnitePickaxeItem(Item.Properties properties) {
        super(
            properties.pickaxe(
                    TheabyssModToolMaterials.mining(TheabyssModToolMaterials.GARNITE, 4, 4.5F),
                    6.5F,
                    -2.5F
                )
        );
    }
}
