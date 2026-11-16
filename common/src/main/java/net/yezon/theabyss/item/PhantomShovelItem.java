package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class PhantomShovelItem extends Item {
    public PhantomShovelItem(Item.Properties properties) {
        super(
            properties.shovel(
                    TheabyssModToolMaterials.mining(TheabyssModToolMaterials.PHANTOM, 0, 9.0F),
                    -1.0F,
                    -2.2F
                )
        );
    }
}
