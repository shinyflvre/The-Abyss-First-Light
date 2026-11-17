package net.yezon.theabyss.fabric.client;

import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

public final class FabricNamespacedPaths {
    private FabricNamespacedPaths() {
    }

    public static @Nullable ResourceLocation resolve(String prefix, String path) {
        if (!path.startsWith(prefix)) {
            return null;
        }
        String rest = path.substring(prefix.length());
        int colon = rest.indexOf(':');
        if (colon <= 0) {
            return null;
        }
        return ResourceLocation.tryBuild(rest.substring(0, colon), prefix + rest.substring(colon + 1));
    }
}
