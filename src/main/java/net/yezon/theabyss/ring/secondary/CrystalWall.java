package net.yezon.theabyss.ring.secondary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.entity.NightbladeSpikeEntity;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.network.NightbladeShakeMessage;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.fx.RingFx;
import net.yezon.theabyss.util.RingMode;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class CrystalWall {
    public static final int SPIKES = 11;
    public static final double GAP = 1.1;
    public static final double DISTANCE = 3.5;
    public static final int HOLD = 100;
    public static final double HEIGHT = 2.6;
    private static final int HURT_EVERY = 10;
    private static final double THICK = 0.9;

    private static final List<Wall> WALLS = new ArrayList<>();

    private record Wall(ServerLevel level, ServerPlayer caster, Vec3 centre, Vec3 along, Vec3 normal, float damage, int[] age,
                        Map<Integer, Long> lastHurt) {
    }

    private CrystalWall() {
    }

    public static double damage(double upgrade) {
        return net.yezon.theabyss.ring.RingDamage.CRYSTAL_WALL.at(upgrade);
    }

    public static double halfLength() {
        return (SPIKES - 1) * GAP * 0.5 + 0.4;
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.level();
        Vec3 normal = player.getLookAngle().multiply(1.0, 0.0, 1.0);
        normal = normal.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : normal.normalize();
        Vec3 along = new Vec3(-normal.z, 0.0, normal.x);
        Vec3 centre = Aim.ground(level, player.position().add(normal.scale(DISTANCE)).add(0.0, 2.0, 0.0), 6);
        float yaw = (float)(Mth.atan2(along.z, along.x) * Mth.RAD_TO_DEG);
        int mid = SPIKES / 2;
        for (int i = 0; i < SPIKES; i++) {
            Vec3 at = Aim.ground(level, centre.add(along.scale((i - mid) * GAP)).add(0.0, 2.0, 0.0), 6);
            NightbladeSpikeEntity spike = TheabyssModEntities.NIGHTBLADE_SPIKE.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (spike == null) {
                continue;
            }
            int warmup = 2 + Math.abs(i - mid) * 2;
            spike.setPos(at.x, at.y, at.z);
            spike.setupWall(player, warmup, HOLD - warmup, yaw + (i % 2 == 0 ? 8.0F : -8.0F));
            level.addFreshEntity(spike);
        }
        WALLS.add(new Wall(level, player, centre, along, normal, (float)damage(vars.ManaDamage), new int[1], new HashMap<>()));
        List<Double> data = new ArrayList<>(SpellFxMessage.points(centre));
        data.add(along.x);
        data.add(along.z);
        SpellFxMessage.around(level, centre, SpellFxMessage.WALL, List.of(player.getId()), data, HOLD);
        RingFx.wave(level, centre.add(0.0, 0.1, 0.0), 5.0F, 14, RingVisuals.ICE);
        NightbladeShakeMessage.around(level, centre, 14.0, 0.6F, 0.3F);
        RingVisuals.sound(level, centre, SoundEvents.AMETHYST_BLOCK_RESONATE, 1.8F, 0.6F);
        RingVisuals.sound(level, centre, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, 0.5F, 1.6F);
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        for (Iterator<Wall> it = WALLS.iterator(); it.hasNext(); ) {
            Wall wall = it.next();
            int age = ++wall.age()[0];
            if (age >= HOLD - 4) {
                it.remove();
                continue;
            }
            if (age >= 3) {
                guard(wall, age);
            }
        }
    }

    private static void guard(Wall wall, int age) {
        ServerLevel level = wall.level();
        double half = halfLength();
        AABB box = new AABB(wall.centre(), wall.centre()).inflate(half + 1.0, HEIGHT + 1.0, half + 1.0);
        long now = level.getGameTime();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != wall.caster() && !e.isSpectator()
            && !RingMode.spares(wall.caster(), e))) {
            Vec3 rel = e.position().subtract(wall.centre());
            double across = rel.dot(wall.along());
            double depth = rel.dot(wall.normal());
            if (Math.abs(across) > half || Math.abs(depth) > THICK + e.getBbWidth() * 0.5 || rel.y > HEIGHT || rel.y < -1.5) {
                continue;
            }
            double side = depth >= 0.0 ? 1.0 : -1.0;
            e.setDeltaMovement(wall.normal().scale(side * 0.7).add(0.0, 0.25, 0.0));
            e.needsSync = true;
            e.syncVelocity = true;
            Long last = wall.lastHurt().get(e.getId());
            if (last == null || now - last >= HURT_EVERY) {
                wall.lastHurt().put(e.getId(), now);
                e.damageCooldownTime = 0;
                e.hurtServer(level, level.damageSources().indirectMagic(wall.caster(), wall.caster()), wall.damage());
                Vec3 at = e.position().add(0.0, e.getBbHeight() * 0.5, 0.0);
                RingFx.burst(level, RingVisuals.sparkle(RingVisuals.ICE, 0.14F, 18).gravity(0.04F), at, 10, 0.2, 0.15);
                RingVisuals.sound(level, at, SoundEvents.AMETHYST_CLUSTER_BREAK, 1.0F, 1.2F);
            }
        }
    }
}
