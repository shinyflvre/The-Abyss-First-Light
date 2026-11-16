package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class CreeperDustItem extends Item {
    public CreeperDustItem(Item.Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON));
    }
}
