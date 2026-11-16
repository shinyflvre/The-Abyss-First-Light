package net.yezon.theabyss.events;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

public class ArenoDamageHandlerEvents {
    public static void execute(LevelAccessor world, Entity entity) {
        if (entity != null) {
            entity.hurtOrSimulate(new DamageSource(world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.LAVA)), 1.0F);
        }
    }
}
