package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class GarniteHoeItem extends Item {
    public GarniteHoeItem(Item.Properties properties) {
        super(
            properties.hoe(TheabyssModToolMaterials.mining(TheabyssModToolMaterials.GARNITE, 0, 0.0F), 1.0F, -2.4F)
                .enchantable(22)
        );
    }
}
