package net.yezon.theabyss.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.yezon.theabyss.ring.SomniumUpgrades;

public class ManaUpgradeResultEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        SomniumUpgrades.drink(entity, SomniumUpgrades.Kind.USAGE);
    }
}
