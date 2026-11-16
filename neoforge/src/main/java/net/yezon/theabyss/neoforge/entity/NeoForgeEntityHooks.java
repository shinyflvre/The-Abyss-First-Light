package net.yezon.theabyss.neoforge.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.EventHooks;
import net.yezon.theabyss.platform.services.EntityHooks;

public final class NeoForgeEntityHooks implements EntityHooks {
    @Override
    public Entity createPart(Entity parent, float width, float height) {
        return new NeoForgePartEntity(parent, width, height);
    }

    @Override
    public boolean onAnimalTame(Animal animal, Player tamer) {
        return EventHooks.onAnimalTame(animal, tamer);
    }
}
