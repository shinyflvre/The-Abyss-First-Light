package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class PhantomPickaxeItem extends Item {
    public PhantomPickaxeItem(Item.Properties properties) {
        super(
            properties.pickaxe(
                    TheabyssModToolMaterials.mining(TheabyssModToolMaterials.PHANTOM, 4, 5.0F),
                    7.5F,
                    -2.3F
                )
        );
    }
}
