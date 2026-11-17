package net.yezon.theabyss.item;

import net.yezon.theabyss.item.TheabyssModToolMaterials.ToolMaterial;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;

public class AberytheAxeItem extends AxeItem {
    private static final ToolMaterial MATERIAL = TheabyssModToolMaterials.mining(TheabyssModToolMaterials.ABERYTHE, 3, 8.5F);

    public AberytheAxeItem(Item.Properties properties) {
        super(MATERIAL, TheabyssModToolMaterials.tool(properties, MATERIAL, 9.0F, -2.8F));
    }
}
