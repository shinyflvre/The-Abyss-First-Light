package net.yezon.theabyss.client.fx.spell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.entity.CrystalNeedleEntity;
import net.yezon.theabyss.entity.CrystalSlashEntity;
import net.yezon.theabyss.entity.RingOfBlackStrikeAttackProjectileEntity;
import net.yezon.theabyss.entity.RingOfFireStrikeAttackProjectileEntity;
import net.yezon.theabyss.ring.RingVisuals;

final class TrailStreaks {
    private static final int MAX_STREAKS = 400;
    private static final int BOLT_RATE = 3;
    private static final int NEEDLE_RATE = 1;
    private static final int CRYSTAL = 0xFF3FBE;
    private static final double MIN_STEP = 0.02;
    private static final double CAMERA_GUARD_SQR = 2.25;
    private static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);
    private static final RandomSource RANDOM = RandomSource.create();
    private static final List<Streak> STREAKS = new ArrayList<>();
    private static final Map<Integer, Followed> FOLLOWED = new HashMap<>();
    private static Vec3 eye = Vec3.ZERO;

    private TrailStreaks() {
    }

    private record Followed(int rgb, long until) {
    }

    private static final class Streak {
        Vec3 base;
        Vec3 dir;
        int rgb;
        float length;
        int life;
        int age;
    }

    static boolean isEmpty() {
        return STREAKS.isEmpty();
    }

    static void clear() {
        STREAKS.clear();
        FOLLOWED.clear();
    }

    static void follow(int id, int rgb, int ticks, long now) {
        FOLLOWED.put(id, new Followed(rgb, now + ticks));
    }

    static void tick(Minecraft mc) {
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        try {
            STREAKS.removeIf(s -> ++s.age >= s.life);
            if (mc.player != null) {
                eye = mc.player.getEyePosition();
            }
            for (Entity e : level.entitiesForRendering()) {
                int rgb = colour(e);
                if (rgb != 0) {
                    emit(new Vec3(e.xo, e.yo, e.zo), e.position(), rgb, e instanceof CrystalNeedleEntity ? NEEDLE_RATE : BOLT_RATE);
                }
            }
            long now = level.getGameTime();
            for (Iterator<Map.Entry<Integer, Followed>> it = FOLLOWED.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Integer, Followed> entry = it.next();
                Entity e = level.getEntity(entry.getKey());
                if (e == null || e.isRemoved() || now > entry.getValue().until()) {
                    it.remove();
                } else {
                    emit(new Vec3(e.xo, e.yo, e.zo), e.position(), entry.getValue().rgb(), BOLT_RATE);
                }
            }
        } catch (RuntimeException e) {
            TheabyssMod.LOGGER.warn("Trail streaks failed", e);
            clear();
        }
    }

    private static int colour(Entity e) {
        if (e instanceof RingOfBlackStrikeAttackProjectileEntity bolt) {
            return bolt.getOwner() instanceof Player ? RingVisuals.BLACKSTRIKE : 0;
        }
        if (e instanceof RingOfFireStrikeAttackProjectileEntity bolt) {
            return bolt.getOwner() instanceof Player ? RingVisuals.EMBER : 0;
        }
        return e instanceof CrystalSlashEntity || e instanceof CrystalNeedleEntity ? CRYSTAL : 0;
    }

    static void emit(Vec3 from, Vec3 to, int rgb, int count) {
        Vec3 step = to.subtract(from);
        double distance = step.length();
        if (distance < MIN_STEP) {
            return;
        }
        Vec3 dir = step.scale(1.0 / distance);
        Vec3 side = dir.cross(UP);
        side = side.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
        Vec3 lift = side.cross(dir);
        for (int i = 0; i < count && STREAKS.size() < MAX_STREAKS; i++) {
            double a = RANDOM.nextDouble() * Math.PI * 2.0;
            double r = 0.1 + RANDOM.nextDouble() * 0.4;
            Vec3 base = from.lerp(to, RANDOM.nextDouble()).add(side.scale(Math.cos(a) * r)).add(lift.scale(Math.sin(a) * r));
            if (base.distanceToSqr(eye) < CAMERA_GUARD_SQR) {
                continue;
            }
            Streak s = new Streak();
            s.base = base;
            s.dir = dir;
            s.rgb = rgb;
            s.length = 1.2F + RANDOM.nextFloat() * 1.6F;
            s.life = 8 + RANDOM.nextInt(9);
            STREAKS.add(s);
        }
    }

    static void draw(Frame f) {
        try {
            for (Streak s : STREAKS) {
                float u = Math.min(1.0F, (s.age + f.pt) / s.life);
                float head = (1.0F - (1.0F - u) * (1.0F - u)) * s.length;
                float tail = Math.max(0.0F, head - s.length * 0.5F);
                float a = Math.min(1.0F, u * 6.0F) * Math.min(1.0F, (1.0F - u) * 3.0F);
                Vec3 top = s.base.add(s.dir.scale(head));
                Vec3 low = s.base.add(s.dir.scale(tail));
                float[] w = {0.012F, 0.05F, 0.07F};
                float[] l = {0.0F, a * 0.55F, a};
                f.ribbon(new Vec3[] {low, low.lerp(top, 0.6), top}, w, l, s.rgb);
                f.flare(top, 0.2F, s.rgb, a * 0.85F);
            }
        } catch (RuntimeException e) {
            TheabyssMod.LOGGER.warn("Trail streaks failed to draw", e);
            clear();
        }
    }
}
