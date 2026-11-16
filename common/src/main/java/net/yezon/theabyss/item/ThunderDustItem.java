package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ThunderDustItem extends Item {
    public ThunderDustItem(Item.Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON));
    }
}
