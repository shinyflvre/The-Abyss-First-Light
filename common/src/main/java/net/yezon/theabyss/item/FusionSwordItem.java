package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class FusionSwordItem extends Item {
    public FusionSwordItem(Item.Properties properties) {
        super(properties.sword(TheabyssModToolMaterials.FUSION, 8.0F, -2.2F));
    }
}
