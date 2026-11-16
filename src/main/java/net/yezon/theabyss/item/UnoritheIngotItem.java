package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class UnoritheIngotItem extends Item {
    public UnoritheIngotItem(Item.Properties properties) {
        super(properties.rarity(Rarity.EPIC));
    }
}
