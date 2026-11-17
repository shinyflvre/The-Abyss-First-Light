package net.yezon.theabyss.platform.client;

import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public final class EntityRenderSuppression {
    private static @Nullable Entity hidden;

    private EntityRenderSuppression() {
    }

    public static void hide(Entity entity) {
        hidden = entity;
    }

    public static boolean isHidden(Entity entity) {
        return hidden == entity;
    }

    public static void clear() {
        hidden = null;
    }
}
