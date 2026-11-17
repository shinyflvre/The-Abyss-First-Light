package net.yezon.theabyss.item;

import net.yezon.theabyss.item.TheabyssModToolMaterials.ToolMaterial;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Item;

public class GarnitePickaxeItem extends PickaxeItem {
    private static final ToolMaterial MATERIAL = TheabyssModToolMaterials.mining(TheabyssModToolMaterials.GARNITE, 4, 4.5F);

    public GarnitePickaxeItem(Item.Properties properties) {
        super(MATERIAL, TheabyssModToolMaterials.tool(properties, MATERIAL, 6.5F, -2.5F));
    }
}
