package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class VitaeDustItem extends Item {
    public VitaeDustItem(Item.Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON));
    }
}
