package net.yezon.theabyss.item;

import net.yezon.theabyss.item.TheabyssModToolMaterials.ToolMaterial;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;

public class KnightHoeItem extends HoeItem {
    private static final ToolMaterial MATERIAL = TheabyssModToolMaterials.mining(TheabyssModToolMaterials.KNIGHT, 0, 0.0F);

    public KnightHoeItem(Item.Properties properties) {
        super(MATERIAL, TheabyssModToolMaterials.tool(properties, MATERIAL, 6.5F, -2.0F));
    }
}
