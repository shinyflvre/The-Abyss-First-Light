package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class PhantomHoeItem extends Item {
    public PhantomHoeItem(Item.Properties properties) {
        super(properties.hoe(TheabyssModToolMaterials.mining(TheabyssModToolMaterials.PHANTOM, 0, 0.0F), 3.5F, -2.2F));
    }
}
