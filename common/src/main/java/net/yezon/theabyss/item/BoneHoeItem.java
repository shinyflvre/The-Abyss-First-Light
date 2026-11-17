package net.yezon.theabyss.item;

import net.yezon.theabyss.item.TheabyssModToolMaterials.ToolMaterial;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;

public class BoneHoeItem extends HoeItem {
    private static final ToolMaterial MATERIAL = TheabyssModToolMaterials.mining(TheabyssModToolMaterials.BONE, 0, 0.0F);

    public BoneHoeItem(Item.Properties properties) {
        super(MATERIAL, TheabyssModToolMaterials.tool(properties, MATERIAL, -1.0F, -3.0F));
    }
}
