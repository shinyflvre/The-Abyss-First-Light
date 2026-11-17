package net.yezon.theabyss.item;

import net.yezon.theabyss.item.TheabyssModToolMaterials.ToolMaterial;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;

public class UnoritheAxeItem extends AxeItem {
    private static final ToolMaterial MATERIAL = TheabyssModToolMaterials.mining(TheabyssModToolMaterials.UNORITHE, 5, 9.5F);

    public UnoritheAxeItem(Item.Properties properties) {
        super(MATERIAL, TheabyssModToolMaterials.tool(properties, MATERIAL, 12.0F, -2.5F));
    }
}
