package net.yezon.theabyss.events;

import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.network.TheabyssModVariables;

public class ManaboostCommandEvents {
    public static void execute(Entity entity) {
        if (entity != null) {
            {
                double _setval = 100.0;
                TheabyssModVariables.updatePlayerVariables(entity, capability -> {
                    capability.Mana = _setval;
                    capability.syncPlayerVariables(entity);
                });
            }
        }
    }
}
