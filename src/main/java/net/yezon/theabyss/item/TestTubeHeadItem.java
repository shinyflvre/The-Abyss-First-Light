package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class TestTubeHeadItem extends Item {
    public TestTubeHeadItem(Item.Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON));
    }
}
