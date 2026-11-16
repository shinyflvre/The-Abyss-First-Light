package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class VitaeStoneItem extends Item {
    public VitaeStoneItem(Item.Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }
}
