package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class KnightSwordItem extends Item {
    public KnightSwordItem(Item.Properties properties) {
        super(properties.sword(TheabyssModToolMaterials.KNIGHT, 15.0F, -2.4F));
    }
}
