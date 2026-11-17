package net.yezon.theabyss.item;

import net.yezon.theabyss.item.TheabyssModToolMaterials.ToolMaterial;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Item;

public class FusionShovelItem extends ShovelItem {
    private static final ToolMaterial MATERIAL = TheabyssModToolMaterials.mining(TheabyssModToolMaterials.FUSION, 0, 8.5F);

    public FusionShovelItem(Item.Properties properties) {
        super(MATERIAL, TheabyssModToolMaterials.tool(properties, MATERIAL, 6.0F, -2.25F));
    }
}
