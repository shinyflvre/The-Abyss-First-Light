package net.yezon.theabyss.events;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.util.RingMode;

public class FirestormOnPotionActiveEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null || !(world instanceof ServerLevel level)) {
            return;
        }
        Vec3 centre = new Vec3(x, y, z);
        DamageSource fire = new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.ON_FIRE));
        float damage = net.yezon.theabyss.ring.RingDamage.FIRESTORM.at(entity);
        for (Entity target : level.getEntitiesOfClass(Entity.class, new AABB(centre, centre).inflate(20.0), e -> e != entity)) {
            if (!RingMode.spares(entity, target)) {
                target.hurt(fire, damage);
                target.igniteForSeconds(5);
            }
        }
        RingVisuals.firestormTick(level, entity);
    }
}
