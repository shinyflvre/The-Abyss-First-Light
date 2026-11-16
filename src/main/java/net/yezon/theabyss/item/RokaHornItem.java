package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class RokaHornItem extends Item {
    public RokaHornItem(Item.Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }
}
