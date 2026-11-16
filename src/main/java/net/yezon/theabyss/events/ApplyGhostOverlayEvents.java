package net.yezon.theabyss.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.init.TheabyssModMobEffects;

public class ApplyGhostOverlayEvents {
    public static boolean execute(Entity entity) {
        return entity == null ? false : entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(TheabyssModMobEffects.GHOST);
    }
}
