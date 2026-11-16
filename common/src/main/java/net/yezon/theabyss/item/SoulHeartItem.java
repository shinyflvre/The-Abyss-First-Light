package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class SoulHeartItem extends Item {
    public SoulHeartItem(Item.Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }
}
