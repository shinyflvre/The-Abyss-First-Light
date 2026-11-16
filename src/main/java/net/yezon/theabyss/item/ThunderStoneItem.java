package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ThunderStoneItem extends Item {
    public ThunderStoneItem(Item.Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }
}
