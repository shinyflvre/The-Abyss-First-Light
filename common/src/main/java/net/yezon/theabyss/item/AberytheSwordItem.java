package net.yezon.theabyss.item;

import net.yezon.theabyss.item.TheabyssModToolMaterials.ToolMaterial;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Item;

public class AberytheSwordItem extends SwordItem {
    private static final ToolMaterial MATERIAL = TheabyssModToolMaterials.ABERYTHE;

    public AberytheSwordItem(Item.Properties properties) {
        super(MATERIAL, TheabyssModToolMaterials.tool(properties, MATERIAL, 7.0F, -2.2F));
    }
}
