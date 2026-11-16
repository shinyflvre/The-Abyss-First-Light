package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ElderEyeItem extends Item {
    public ElderEyeItem(Item.Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }
}
