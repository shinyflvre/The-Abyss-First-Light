package net.yezon.theabyss.fabric.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.yezon.theabyss.platform.client.settings.KeyConflictContext;
import net.yezon.theabyss.platform.client.settings.KeyModifier;
import net.yezon.theabyss.platform.services.GuiPlatformHooks;

public final class FabricGuiPlatformHooks implements GuiPlatformHooks {
    @Override
    public KeyMapping keyMapping(String name, KeyConflictContext context, KeyModifier modifier, InputConstants.Type type, int keyCode, String category) {
        return new KeyMapping(name, type, keyCode, category);
    }

    @Override
    public int hudLeftHeight() {
        return FabricHudHeights.left();
    }

    @Override
    public void reserveHudRight(int minHeight) {
        FabricHudHeights.reserveRight(minHeight);
    }
}
