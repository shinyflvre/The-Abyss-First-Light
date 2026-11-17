package net.yezon.theabyss.item;

import net.yezon.theabyss.item.TheabyssModToolMaterials.ToolMaterial;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;

public class GarniteAxeItem extends AxeItem {
    private static final ToolMaterial MATERIAL = TheabyssModToolMaterials.mining(TheabyssModToolMaterials.GARNITE, 4, 9.0F);

    public GarniteAxeItem(Item.Properties properties) {
        super(MATERIAL, TheabyssModToolMaterials.tool(properties, MATERIAL, 9.5F, -2.6F));
    }
}
