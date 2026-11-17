package net.yezon.theabyss.item;

import net.yezon.theabyss.item.TheabyssModToolMaterials.ToolMaterial;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Item;

public class UnoritheSwordItem extends SwordItem {
    private static final ToolMaterial MATERIAL = TheabyssModToolMaterials.UNORITHE;

    public UnoritheSwordItem(Item.Properties properties) {
        super(MATERIAL, TheabyssModToolMaterials.tool(properties, MATERIAL, 10.0F, -2.0F));
    }
}
