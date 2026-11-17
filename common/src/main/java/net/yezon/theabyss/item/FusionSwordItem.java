package net.yezon.theabyss.item;

import net.yezon.theabyss.item.TheabyssModToolMaterials.ToolMaterial;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Item;

public class FusionSwordItem extends SwordItem {
    private static final ToolMaterial MATERIAL = TheabyssModToolMaterials.FUSION;

    public FusionSwordItem(Item.Properties properties) {
        super(MATERIAL, TheabyssModToolMaterials.tool(properties, MATERIAL, 8.0F, -2.2F));
    }
}
