package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class LoranIngotItem extends Item {
    public LoranIngotItem(Item.Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON));
    }
}
