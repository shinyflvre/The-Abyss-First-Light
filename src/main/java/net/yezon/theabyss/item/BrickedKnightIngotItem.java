package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class BrickedKnightIngotItem extends Item {
    public BrickedKnightIngotItem(Item.Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }
}
