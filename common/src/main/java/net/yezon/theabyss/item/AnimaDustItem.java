package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class AnimaDustItem extends Item {
    public AnimaDustItem(Item.Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }
}
