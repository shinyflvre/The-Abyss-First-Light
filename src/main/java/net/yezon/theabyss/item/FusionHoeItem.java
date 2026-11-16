package net.yezon.theabyss.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;

public class FusionHoeItem extends Item {
    public FusionHoeItem(Item.Properties properties) {
        super(
            properties.hoe(TheabyssModToolMaterials.mining(TheabyssModToolMaterials.FUSION, 0, 0.0F), 2.0F, -2.25F)
                .component(DataComponents.ENCHANTABLE, null)
        );
    }
}
