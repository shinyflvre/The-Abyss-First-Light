package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class LurkerSobberItem extends Item {
    public LurkerSobberItem(Item.Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }
}
