package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class EYEICONItem extends Item {
    public EYEICONItem(Item.Properties properties) {
        super(properties.stacksTo(8).rarity(Rarity.RARE));
    }
}
