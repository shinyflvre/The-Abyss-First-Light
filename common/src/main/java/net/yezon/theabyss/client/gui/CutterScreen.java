package net.yezon.theabyss.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yezon.theabyss.world.inventory.CutterMenu;

public class CutterScreen extends MachineScreen<CutterMenu> {
    public CutterScreen(CutterMenu container, Inventory inventory, Component text) {
        super(container, inventory, text, ResourceLocation.fromNamespaceAndPath("theabyss", "textures/screens/crystalcutter_gui.png"),
            Component.translatable("gui.theabyss.cutter.label_crystal_cutter"), -6684724);
    }
}
