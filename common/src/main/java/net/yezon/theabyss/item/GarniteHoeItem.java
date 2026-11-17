package net.yezon.theabyss.item;

import net.yezon.theabyss.item.TheabyssModToolMaterials.ToolMaterial;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;

public class GarniteHoeItem extends HoeItem {
    private static final ToolMaterial MATERIAL = TheabyssModToolMaterials.mining(TheabyssModToolMaterials.GARNITE, 0, 0.0F);

    public GarniteHoeItem(Item.Properties properties) {
        super(MATERIAL, TheabyssModToolMaterials.tool(properties, MATERIAL, 1.0F, -2.4F));
    }

    @Override
    public int getEnchantmentValue() {
        return 22;
    }
}
