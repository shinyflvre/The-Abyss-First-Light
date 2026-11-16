package net.yezon.theabyss.platform.services;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public interface EntityHooks {
    static EntityHooks get() {
        EntityHooks hooks = Instance.hooks;
        if (hooks == null) {
            throw new IllegalStateException("The Abyss entity hooks were not installed by the loader");
        }
        return hooks;
    }

    static void install(EntityHooks hooks) {
        Instance.hooks = hooks;
    }

    Entity createPart(Entity parent, float width, float height);

    boolean onAnimalTame(Animal animal, Player tamer);

    final class Instance {
        private static volatile @Nullable EntityHooks hooks;

        private Instance() {
        }
    }
}
