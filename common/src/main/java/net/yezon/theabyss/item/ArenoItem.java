package net.yezon.theabyss.item;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.yezon.theabyss.init.TheabyssModFluids;

public class ArenoItem extends BucketItem {
    public ArenoItem(Item.Properties properties) {
        super(TheabyssModFluids.ARENO.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1));
    }
}
