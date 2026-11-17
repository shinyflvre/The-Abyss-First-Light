package net.yezon.theabyss.item;

import net.yezon.theabyss.item.TheabyssModToolMaterials.ToolMaterial;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;

public class AberytheHoeItem extends HoeItem {
    private static final ToolMaterial MATERIAL = TheabyssModToolMaterials.mining(TheabyssModToolMaterials.ABERYTHE, 0, 0.0F);

    public AberytheHoeItem(Item.Properties properties) {
        super(MATERIAL, TheabyssModToolMaterials.tool(properties, MATERIAL, 0.0F, -2.5F));
    }
}
