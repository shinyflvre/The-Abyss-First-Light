package net.yezon.theabyss.fabric.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.platform.services.EntityHooks;

public final class FabricEntityHooks implements EntityHooks {
    @Override
    public Entity createPart(Entity parent, float width, float height) {
        return new FabricPartEntity(parent, width, height);
    }

    @Override
    public boolean onAnimalTame(Animal animal, Player tamer) {
        return false;
    }
}
