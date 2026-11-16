package net.yezon.theabyss.item;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.yezon.theabyss.init.TheabyssModFluids;

public class InfusedLavaItem extends BucketItem {
    public InfusedLavaItem(Item.Properties properties) {
        super(TheabyssModFluids.INFUSED_LAVA.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1));
    }
}
