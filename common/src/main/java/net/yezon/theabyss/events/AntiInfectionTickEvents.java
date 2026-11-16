package net.yezon.theabyss.events;

import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.network.TheabyssModVariables;

public final class AntiInfectionTickEvents {
    private static final double CURE_LIMIT = 0.05;
    private static final double CURE_RATE = 0.005;

    private AntiInfectionTickEvents() {
    }

    public static void execute(Entity entity) {
        if (entity == null) {
            return;
        }
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(entity);
        if (vars.Infected > CURE_LIMIT) {
            vars.Infected -= CURE_RATE;
        }
    }
}
