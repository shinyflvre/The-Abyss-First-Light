package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class PhantomAxeItem extends Item {
    public PhantomAxeItem(Item.Properties properties) {
        super(properties.axe(TheabyssModToolMaterials.mining(TheabyssModToolMaterials.PHANTOM, 5, 9.0F), 9.0F, -2.3F));
    }
}
