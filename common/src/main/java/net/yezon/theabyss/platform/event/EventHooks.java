package net.yezon.theabyss.platform.event;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.platform.services.EntityHooks;

public final class EventHooks {
    private EventHooks() {
    }

    public static boolean onAnimalTame(Animal animal, Player tamer) {
        return EntityHooks.get().onAnimalTame(animal, tamer);
    }
}
