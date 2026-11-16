package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class CrystalHandItem extends Item {
    public CrystalHandItem(Item.Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }
}
