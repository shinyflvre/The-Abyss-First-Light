package net.yezon.theabyss.ring.secondary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.tick.ServerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.fx.RingFx;
import net.yezon.theabyss.util.RingMode;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class FlameDash {
    public static final double SPEED = 1.7;
    public static final int TICKS = 5;
    public static final int TRAIL = 80;
    private static final double TRAIL_WIDTH = 0.9;

    private record Trail(ServerLevel level, ServerPlayer caster, List<Vec3> path, float damage, int[] age, Map<Integer, Long> burnt) {
    }

    private static final List<Trail> TRAILS = new ArrayList<>();

    private FlameDash() {
    }

    public static double damage(double upgrade) {
        return 3.0 + upgrade;
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.level();
        Vec3 dir = Dash.ahead(player);
        float hit = (float)damage(vars.ManaDamage);
        float burn = (float)(1.0 + 0.5 * vars.ManaDamage);
        Set<Integer> cut = new HashSet<>();
        SpellFxMessage.around(level, player.position(), SpellFxMessage.DASH, List.of(player.getId()), List.of((double)RingVisuals.FIRE), TICKS);
        RingFx.wave(level, player.position().add(0.0, 0.1, 0.0), 3.5F, 10, RingVisuals.FIRE);
        RingVisuals.sound(level, player.position(), SoundEvents.BLAZE_SHOOT, 1.4F, 0.7F);
        RingVisuals.sound(level, player.position(), SoundEvents.FIRECHARGE_USE, 1.2F, 0.8F);
        Dash.run(player, dir, SPEED, TICKS, at -> {
            RingFx.burst(level, RingVisuals.flame(RingVisuals.FIRE, 0.4F, 12).gravity(-0.02F), at.add(0.0, 0.6, 0.0), 6, 0.3, 0.04);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(1.4, 1.2, 1.4).move(0.0, 0.8, 0.0),
                v -> v != player && v.isAlive() && !RingMode.spares(player, v) && !cut.contains(v.getId()))) {
                cut.add(e.getId());
                e.damageCooldownTime = 0;
                e.hurtServer(level, level.damageSources().indirectMagic(player, player), hit);
                e.igniteForSeconds(4.0F);
                Vec3 aside = e.position().subtract(at).multiply(1.0, 0.0, 1.0);
                aside = aside.lengthSqr() < 1.0E-4 ? new Vec3(-dir.z, 0.0, dir.x) : aside.normalize();
                e.push(aside.x * 0.8, 0.35, aside.z * 0.8);
                RingVisuals.sound(level, e.position(), SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.7F);
            }
        }, path -> {
            TRAILS.add(new Trail(level, player, path, burn, new int[1], new HashMap<>()));
            SpellFxMessage.around(level, player.position(), SpellFxMessage.FLAME_TRAIL, List.of(player.getId()),
                SpellFxMessage.points(path.toArray(new Vec3[0])), TRAIL);
            List<Double> ghosts = new ArrayList<>();
            ghosts.add((double)RingVisuals.FIRE);
            for (Vec3 p : path) {
                ghosts.add(p.x);
                ghosts.add(p.y);
                ghosts.add(p.z);
                ghosts.add((double)player.getYRot());
            }
            SpellFxMessage.around(level, player.position(), SpellFxMessage.GHOSTS, List.of(player.getId()), ghosts, 16);
            RingVisuals.sound(level, player.position(), SoundEvents.FIRE_AMBIENT, 1.6F, 0.8F);
        });
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (Iterator<Trail> it = TRAILS.iterator(); it.hasNext(); ) {
            Trail trail = it.next();
            int age = ++trail.age()[0];
            if (age > TRAIL) {
                it.remove();
                continue;
            }
            if (age % 5 != 0) {
                continue;
            }
            List<Vec3> path = trail.path();
            AABB box = new AABB(path.get(0), path.get(path.size() - 1)).inflate(TRAIL_WIDTH + 0.5, 1.5, TRAIL_WIDTH + 0.5);
            long now = trail.level().getGameTime();
            for (LivingEntity e : trail.level().getEntitiesOfClass(LivingEntity.class, box, v -> v.isAlive() && v != trail.caster()
                && !RingMode.spares(trail.caster(), v))) {
                if (distance(path, e.position()) > TRAIL_WIDTH) {
                    continue;
                }
                Long last = trail.burnt().get(e.getId());
                if (last != null && now - last < 10) {
                    continue;
                }
                trail.burnt().put(e.getId(), now);
                e.igniteForSeconds(4.0F);
                e.hurtServer(trail.level(), trail.level().damageSources().inFire(), trail.damage());
            }
        }
    }

    private static double distance(List<Vec3> path, Vec3 p) {
        double best = Double.MAX_VALUE;
        for (int i = 0; i + 1 < path.size(); i++) {
            Vec3 a = path.get(i);
            Vec3 b = path.get(i + 1);
            if (Math.abs(p.y - (a.y + b.y) * 0.5) > 1.5) {
                continue;
            }
            Vec3 ab = new Vec3(b.x - a.x, 0.0, b.z - a.z);
            Vec3 ap = new Vec3(p.x - a.x, 0.0, p.z - a.z);
            double len = ab.lengthSqr();
            double t = len < 1.0E-6 ? 0.0 : Math.max(0.0, Math.min(1.0, ap.dot(ab) / len));
            best = Math.min(best, ap.subtract(ab.scale(t)).length());
        }
        return best;
    }
}
