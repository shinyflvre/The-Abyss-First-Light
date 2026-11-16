package net.yezon.theabyss.events;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModMobEffects;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.util.RingMode;

public class ApplyGhostFireAttackEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (!(world instanceof ServerLevel level) || !(entity instanceof LivingEntity ghost) || !ghost.hasEffect(TheabyssModMobEffects.GHOST)) {
            return;
        }
        Vec3 centre = new Vec3(x, y, z);
        DamageSource fire = new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.ON_FIRE));
        List<LivingEntity> hit = new ArrayList<>();
        float damage = net.yezon.theabyss.ring.RingDamage.GHOST_FIRE.at(ghost);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(centre, centre).inflate(7.5), e -> e != entity)) {
            if (!RingMode.spares(entity, target)) {
                target.igniteForSeconds(15);
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false));
                target.hurtServer(level, fire, damage);
                hit.add(target);
            }
        }
        RingVisuals.fire(level, centre, ghost, hit);
    }
}
