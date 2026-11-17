package net.yezon.theabyss.neoforge.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.yezon.theabyss.platform.client.settings.KeyConflictContext;
import net.yezon.theabyss.platform.client.settings.KeyModifier;
import net.yezon.theabyss.platform.services.GuiPlatformHooks;

public final class NeoForgeGuiPlatformHooks implements GuiPlatformHooks {
    @Override
    public KeyMapping keyMapping(String name, KeyConflictContext context, KeyModifier modifier, InputConstants.Type type, int keyCode, String category) {
        return new KeyMapping(name, conflictContext(context), keyModifier(modifier), type, keyCode, category);
    }

    private static net.neoforged.neoforge.client.settings.KeyConflictContext conflictContext(KeyConflictContext context) {
        return switch (context) {
            case UNIVERSAL -> net.neoforged.neoforge.client.settings.KeyConflictContext.UNIVERSAL;
            case GUI -> net.neoforged.neoforge.client.settings.KeyConflictContext.GUI;
            case IN_GAME -> net.neoforged.neoforge.client.settings.KeyConflictContext.IN_GAME;
        };
    }

    private static net.neoforged.neoforge.client.settings.KeyModifier keyModifier(KeyModifier modifier) {
        return switch (modifier) {
            case CONTROL -> net.neoforged.neoforge.client.settings.KeyModifier.CONTROL;
            case SHIFT -> net.neoforged.neoforge.client.settings.KeyModifier.SHIFT;
            case ALT -> net.neoforged.neoforge.client.settings.KeyModifier.ALT;
            case NONE -> net.neoforged.neoforge.client.settings.KeyModifier.NONE;
        };
    }

    @Override
    public int hudLeftHeight() {
        return Minecraft.getInstance().gui.leftHeight;
    }

    @Override
    public void reserveHudRight(int minHeight) {
        Gui gui = Minecraft.getInstance().gui;
        gui.rightHeight = Math.max(gui.rightHeight, minHeight);
    }
}
