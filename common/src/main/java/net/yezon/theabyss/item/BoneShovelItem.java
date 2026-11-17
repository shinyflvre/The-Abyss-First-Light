package net.yezon.theabyss.item;

import net.yezon.theabyss.item.TheabyssModToolMaterials.ToolMaterial;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Item;

public class BoneShovelItem extends ShovelItem {
    private static final ToolMaterial MATERIAL = TheabyssModToolMaterials.mining(TheabyssModToolMaterials.BONE, 0, 7.0F);

    public BoneShovelItem(Item.Properties properties) {
        super(MATERIAL, TheabyssModToolMaterials.tool(properties, MATERIAL, 3.0F, -3.0F));
    }
}
