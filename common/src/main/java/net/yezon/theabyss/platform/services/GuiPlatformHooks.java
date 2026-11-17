package net.yezon.theabyss.platform.services;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.yezon.theabyss.platform.client.settings.KeyConflictContext;
import net.yezon.theabyss.platform.client.settings.KeyModifier;
import org.jspecify.annotations.Nullable;

public interface GuiPlatformHooks {
    static GuiPlatformHooks get() {
        GuiPlatformHooks hooks = Instance.hooks;
        if (hooks == null) {
            throw new IllegalStateException("The Abyss GUI hooks were not installed by the loader");
        }
        return hooks;
    }

    static void install(GuiPlatformHooks hooks) {
        Instance.hooks = hooks;
    }

    KeyMapping keyMapping(String name, KeyConflictContext context, KeyModifier modifier, InputConstants.Type type, int keyCode, String category);

    int hudLeftHeight();

    void reserveHudRight(int minHeight);

    final class Instance {
        private static volatile @Nullable GuiPlatformHooks hooks;

        private Instance() {
        }
    }
}
