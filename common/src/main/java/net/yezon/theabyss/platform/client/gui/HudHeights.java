package net.yezon.theabyss.platform.client.gui;

import net.yezon.theabyss.platform.services.ClientPlatformHooks;

public final class HudHeights {
    private HudHeights() {
    }

    public static int left() {
        return ClientPlatformHooks.get().hudLeftHeight();
    }

    public static void reserveRight(int minHeight) {
        ClientPlatformHooks.get().reserveHudRight(minHeight);
    }
}
