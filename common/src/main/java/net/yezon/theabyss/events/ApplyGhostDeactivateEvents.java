package net.yezon.theabyss.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.init.TheabyssModMobEffects;

public class ApplyGhostDeactivateEvents {
    public static void execute(Entity entity) {
        if (entity != null) {
            if (entity instanceof LivingEntity _entity) {
                _entity.removeEffect(TheabyssModMobEffects.GHOST.getDelegate());
            }
        }
    }
}
