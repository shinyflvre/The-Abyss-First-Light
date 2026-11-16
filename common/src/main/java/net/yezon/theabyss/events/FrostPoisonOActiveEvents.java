package net.yezon.theabyss.events;

import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.network.TheabyssModVariables;

public class FrostPoisonOActiveEvents {
    public static void execute(Entity entity) {
        if (entity != null) {
            if (TheabyssModVariables.getPlayerVariables(entity).Freeze < 1.0) {
                {
                    double _setval = TheabyssModVariables.getPlayerVariables(entity)
                            .Freeze
                        + 0.0125;
                    TheabyssModVariables.updatePlayerVariables(entity, capability -> {
                        capability.Freeze = _setval;
                        capability.syncPlayerVariables(entity);
                    });
                }
            }
        }
    }
}
