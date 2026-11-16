package net.yezon.theabyss.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.yezon.theabyss.world.inventory.ExtractorMenu;

public class ExtractorScreen extends MachineScreen<ExtractorMenu> {
    public ExtractorScreen(ExtractorMenu container, Inventory inventory, Component text) {
        super(container, inventory, text, Identifier.fromNamespaceAndPath("theabyss", "textures/screens/extractor_gui.png"),
            Component.translatable("gui.theabyss.extractor.label_extractor"), -3342388);
    }
}
