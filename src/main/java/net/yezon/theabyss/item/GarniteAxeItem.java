package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class GarniteAxeItem extends Item {
    public GarniteAxeItem(Item.Properties properties) {
        super(properties.axe(TheabyssModToolMaterials.mining(TheabyssModToolMaterials.GARNITE, 4, 9.0F), 9.5F, -2.6F));
    }
}
