package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;

public class BoneShovelItem extends Item {
    public BoneShovelItem(Item.Properties properties) {
        super(properties.shovel(TheabyssModToolMaterials.mining(TheabyssModToolMaterials.BONE, 0, 7.0F), 3.0F, -3.0F));
    }
}
