package net.yezon.theabyss.platform.client.renderstate;

import net.minecraft.util.context.ContextKey;
import net.yezon.theabyss.platform.services.ClientPlatformHooks;
import org.jspecify.annotations.Nullable;

public final class RenderData {
    private RenderData() {
    }

    public static <T> @Nullable T get(Object renderState, ContextKey<T> key) {
        return ClientPlatformHooks.get().getRenderData(renderState, key);
    }

    public static <T> void set(Object renderState, ContextKey<T> key, @Nullable T value) {
        ClientPlatformHooks.get().setRenderData(renderState, key, value);
    }
}
