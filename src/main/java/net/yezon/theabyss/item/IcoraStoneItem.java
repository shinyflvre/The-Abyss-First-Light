package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class IcoraStoneItem extends Item {
    public IcoraStoneItem(Item.Properties properties) {
        super(properties.rarity(Rarity.EPIC));
    }
}
