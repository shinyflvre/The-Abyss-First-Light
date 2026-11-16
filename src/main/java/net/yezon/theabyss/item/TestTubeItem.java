package net.yezon.theabyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class TestTubeItem extends Item {
    public TestTubeItem(Item.Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON));
    }
}
