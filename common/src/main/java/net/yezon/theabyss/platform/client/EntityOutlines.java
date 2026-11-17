package net.yezon.theabyss.platform.client;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.ToIntFunction;
import net.minecraft.world.entity.Entity;

public final class EntityOutlines {
    private static final List<ToIntFunction<Entity>> SOURCES = new CopyOnWriteArrayList<>();

    private EntityOutlines() {
    }

    public static void register(ToIntFunction<Entity> source) {
        SOURCES.add(source);
    }

    public static int colorOf(Entity entity) {
        for (ToIntFunction<Entity> source : SOURCES) {
            int colour = source.applyAsInt(entity);
            if (colour != 0) {
                return colour;
            }
        }
        return 0;
    }
}
