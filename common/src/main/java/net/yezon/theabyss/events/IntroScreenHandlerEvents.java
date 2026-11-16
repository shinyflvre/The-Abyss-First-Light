package net.yezon.theabyss.events;

import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.network.TheabyssModVariables;

public class IntroScreenHandlerEvents {
    public static boolean execute(Entity entity) {
        return entity == null
            ? false
            : !TheabyssModVariables.getPlayerVariables(entity).EA_Intro
                && TheabyssModVariables.getPlayerVariables(entity).EternalAbyss;
    }
}
