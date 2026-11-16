package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class AnimaNuggetItem extends Item {
    public AnimaNuggetItem(Item.Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }
}
