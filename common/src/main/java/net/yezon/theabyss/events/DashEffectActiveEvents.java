package net.yezon.theabyss.events;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public final class DashEffectActiveEvents {
    private static final double RADIUS = 1.5;
    private static final float DAMAGE = 20.0F;

    private DashEffectActiveEvents() {
    }

    public static void execute(LivingEntity dasher) {
        if (!(dasher.level() instanceof ServerLevel level)) {
            return;
        }
        DamageSource source = new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.GENERIC));
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(dasher.position(), dasher.position()).inflate(RADIUS),
            target -> target != dasher && target.isAlive())) {
            target.hurt(source, DAMAGE);
            target.igniteForSeconds(1);
        }
    }
}
