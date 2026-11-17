package net.yezon.theabyss.platform.client.gui;

import net.yezon.theabyss.platform.services.GuiPlatformHooks;

public final class HudHeights {
    private HudHeights() {
    }

    public static int left() {
        return GuiPlatformHooks.get().hudLeftHeight();
    }

    public static void reserveRight(int minHeight) {
        GuiPlatformHooks.get().reserveHudRight(minHeight);
    }
}
