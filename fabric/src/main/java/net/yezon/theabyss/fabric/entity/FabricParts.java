package net.yezon.theabyss.fabric.entity;

import java.util.Collection;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.platform.entity.MultipartEntity;
import org.jspecify.annotations.Nullable;

public final class FabricParts {
    private FabricParts() {
    }

    public static void init() {
        ServerEntityEvents.ENTITY_LOAD.register(FabricParts::track);
        ServerEntityEvents.ENTITY_UNLOAD.register(FabricParts::untrack);
    }

    public static void track(Entity entity, Level level) {
        if (entity instanceof MultipartEntity multipart) {
            for (Entity part : multipart.abyssParts()) {
                ((FabricPartLevel)level).theabyss$parts().put(part.getId(), part);
            }
        }
    }

    public static void untrack(Entity entity, Level level) {
        if (entity instanceof MultipartEntity multipart) {
            for (Entity part : multipart.abyssParts()) {
                ((FabricPartLevel)level).theabyss$parts().remove(part.getId());
            }
        }
    }

    public static Collection<Entity> parts(Level level) {
        return ((FabricPartLevel)level).theabyss$parts().values();
    }

    public static @Nullable Entity part(Level level, int id) {
        return ((FabricPartLevel)level).theabyss$parts().get(id);
    }

    public static @Nullable Entity parent(@Nullable Entity entity) {
        return entity instanceof FabricPartEntity part ? part.getParent() : null;
    }
}
