package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class AberytheSwordItem extends Item {
    public AberytheSwordItem(Item.Properties properties) {
        super(properties.sword(TheabyssModToolMaterials.ABERYTHE, 7.0F, -2.2F));
    }
}
