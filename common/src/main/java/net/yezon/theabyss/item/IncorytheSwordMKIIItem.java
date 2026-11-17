package net.yezon.theabyss.item;

import net.yezon.theabyss.item.TheabyssModToolMaterials.ToolMaterial;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Item;

public class IncorytheSwordMKIIItem extends SwordItem {
    private static final ToolMaterial MATERIAL = TheabyssModToolMaterials.INCORYTHE;

    public IncorytheSwordMKIIItem(Item.Properties properties) {
        super(MATERIAL, TheabyssModToolMaterials.tool(properties, MATERIAL, 13.5F, -1.8F));
    }
}
