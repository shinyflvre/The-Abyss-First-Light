package net.yezon.theabyss.item;

import net.yezon.theabyss.item.TheabyssModToolMaterials.ToolMaterial;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;

public class FusionAxeItem extends AxeItem {
    private static final ToolMaterial MATERIAL = TheabyssModToolMaterials.mining(TheabyssModToolMaterials.FUSION, 4, 9.0F);

    public FusionAxeItem(Item.Properties properties) {
        super(MATERIAL, TheabyssModToolMaterials.tool(properties, MATERIAL, 10.0F, -2.6F));
    }
}
