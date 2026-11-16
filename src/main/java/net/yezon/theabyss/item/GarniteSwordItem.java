package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class GarniteSwordItem extends Item {
    public GarniteSwordItem(Item.Properties properties) {
        super(properties.sword(TheabyssModToolMaterials.GARNITE, 7.5F, -2.2F));
    }
}
