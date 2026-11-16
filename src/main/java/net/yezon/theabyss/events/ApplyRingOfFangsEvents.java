package net.yezon.theabyss.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.entity.NightbladeSpikeEntity;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.ring.RingCast;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.util.RingMode;

public final class ApplyRingOfFangsEvents {
    private static final double COST = 50.0;
    private static final int COOLDOWN = 140;
    private static final double RANGE = 17.5;
    private static final double[][] RING_A = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}};
    private static final double[][] RING_B = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
    private static final double[][] RING_C = {{3, 0}, {-3, 0}, {0, 3}, {0, -3}, {2, -2}, {2, 2}, {-2, 2}, {-2, -2}};
    private static final double[][] RING_D = {
        {4, -1}, {4, 1}, {3, 3}, {1, 4}, {-1, 4}, {-3, 3}, {-4, 1}, {-4, -1}, {-3, -3}, {-1, -4}, {1, -4}, {3, -3}
    };
    private static final double[][] RING_E = {{-5, 0}, {5, 0}, {0, 5}, {0, -5}};
    private static final double[][][] WAVES = {RING_A, RING_B, RING_C, RING_D, RING_E, RING_B, RING_C, RING_D};
    private static final int[] WAVE_DELAYS = {0, 5, 10, 15, 20, 25, 30, 35};

    private ApplyRingOfFangsEvents() {
    }

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        RingCast cast = RingCast.start(world, entity, itemstack, COST, new Vec3(x, y, z));
        if (cast == null) {
            return;
        }
        cast.durability(TheabyssModItems.RING_OF_FANGS.get(), 1).cooldown(COOLDOWN);
        cast.charge(COST);
        if (!(world instanceof ServerLevel level) || !(entity instanceof LivingEntity caster)) {
            return;
        }
        Vec3 centre = new Vec3(x, y, z);
        float damage = (float)net.yezon.theabyss.ring.RingDamage.CRYSTAL_FANGS.at(cast.vars().ManaDamage);
        RingVisuals.fangs(level, centre);
        for (int wave = 0; wave < WAVES.length; wave++) {
            double[][] offsets = WAVES[wave];
            if (WAVE_DELAYS[wave] == 0) {
                summonRing(level, centre, offsets, caster, damage);
            } else {
                TheabyssMod.queueServerWork(WAVE_DELAYS[wave], () -> summonRing(level, centre, offsets, caster, damage));
            }
        }
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(centre, centre).inflate(RANGE),
            target -> target != caster && !RingMode.spares(caster, target))) {
            summonFang(level, target.getX(), target.getY(), target.getZ(), caster, damage);
        }
    }

    private static void summonRing(ServerLevel level, Vec3 centre, double[][] offsets, LivingEntity caster, float damage) {
        for (double[] offset : offsets) {
            summonFang(level, centre.x + offset[0], centre.y, centre.z + offset[1], caster, damage);
        }
    }

    private static void summonFang(ServerLevel level, double x, double y, double z, LivingEntity caster, float damage) {
        NightbladeSpikeEntity spike = TheabyssModEntities.NIGHTBLADE_SPIKE.get().create(level, EntitySpawnReason.TRIGGERED);
        if (spike != null) {
            spike.setPos(x, y, z);
            spike.setupRing(caster, 4, damage, (float)(caster.getRandom().nextDouble() * 360.0));
            level.addFreshEntity(spike);
        }
    }
}
