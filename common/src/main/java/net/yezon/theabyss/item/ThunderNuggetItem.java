package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ThunderNuggetItem extends Item {
    public ThunderNuggetItem(Item.Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON));
    }
}
