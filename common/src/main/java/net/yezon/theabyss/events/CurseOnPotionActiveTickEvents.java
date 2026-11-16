package net.yezon.theabyss.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.yezon.theabyss.ring.RingVisuals;

public class CurseOnPotionActiveTickEvents {
    public static void execute(LevelAccessor world, Entity entity) {
        if (world instanceof ServerLevel level && entity != null) {
            RingVisuals.cursedTick(level, entity);
        }
    }
}
