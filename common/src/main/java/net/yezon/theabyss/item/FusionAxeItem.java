package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class FusionAxeItem extends Item {
    public FusionAxeItem(Item.Properties properties) {
        super(properties.axe(TheabyssModToolMaterials.mining(TheabyssModToolMaterials.FUSION, 4, 9.0F), 10.0F, -2.6F));
    }
}
