package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class IcoraNuggetItem extends Item {
    public IcoraNuggetItem(Item.Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }
}
