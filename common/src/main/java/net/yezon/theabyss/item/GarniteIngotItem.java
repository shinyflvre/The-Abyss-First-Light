package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class GarniteIngotItem extends Item {
    public GarniteIngotItem(Item.Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }
}
