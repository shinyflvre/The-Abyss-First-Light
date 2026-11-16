package net.yezon.theabyss.events;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;

public class MagicianSpawnEvents {
    public static boolean execute(LevelAccessor world) {
        return isRaining(world);
    }

    static boolean isRaining(LevelAccessor world) {
        if (world instanceof ServerLevelAccessor serverWorld) {
            return serverWorld.getLevel().getWeatherData().isRaining();
        }
        return world instanceof Level level && level.isRaining();
    }
}
