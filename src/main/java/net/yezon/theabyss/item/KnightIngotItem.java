package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class KnightIngotItem extends Item {
    public KnightIngotItem(Item.Properties properties) {
        super(properties.rarity(Rarity.EPIC));
    }
}
