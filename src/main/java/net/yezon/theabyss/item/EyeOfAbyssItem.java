package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class EyeOfAbyssItem extends Item {
    public EyeOfAbyssItem(Item.Properties properties) {
        super(properties.rarity(Rarity.EPIC));
    }
}
