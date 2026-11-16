package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class VitaeNuggetItem extends Item {
    public VitaeNuggetItem(Item.Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON));
    }
}
