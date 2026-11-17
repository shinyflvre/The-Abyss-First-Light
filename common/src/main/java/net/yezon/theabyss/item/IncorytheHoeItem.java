package net.yezon.theabyss.item;

import net.yezon.theabyss.item.TheabyssModToolMaterials.ToolMaterial;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;

public class IncorytheHoeItem extends HoeItem {
    private static final ToolMaterial MATERIAL = TheabyssModToolMaterials.mining(TheabyssModToolMaterials.INCORYTHE, 0, 0.0F);

    public IncorytheHoeItem(Item.Properties properties) {
        super(MATERIAL, TheabyssModToolMaterials.tool(properties, MATERIAL, 6.0F, -2.0F));
    }
}
