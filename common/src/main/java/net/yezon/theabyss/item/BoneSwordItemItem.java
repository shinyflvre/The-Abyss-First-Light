package net.yezon.theabyss.item;

import net.yezon.theabyss.item.TheabyssModToolMaterials.ToolMaterial;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Item;

public class BoneSwordItemItem extends SwordItem {
    private static final ToolMaterial MATERIAL = TheabyssModToolMaterials.BONE;

    public BoneSwordItemItem(Item.Properties properties) {
        super(MATERIAL, TheabyssModToolMaterials.tool(properties, MATERIAL, 4.0F, -2.0F));
    }
}
