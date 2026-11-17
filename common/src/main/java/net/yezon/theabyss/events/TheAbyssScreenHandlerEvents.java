package net.yezon.theabyss.events;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class TheAbyssScreenHandlerEvents {
    public static boolean execute(Entity entity) {
        return entity == null
            ? false
            : entity.level().dimension() == ResourceKey.<Level>create(Registries.DIMENSION, ResourceLocation.parse("theabyss:the_abyss"))
                || entity.level().dimension() == Level.OVERWORLD;
    }
}
