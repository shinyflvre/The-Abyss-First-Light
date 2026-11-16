package net.yezon.theabyss.client.fx;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RootCrackPattern {
    public static final int RIM = 0;
    public static final int HALO = 1;
    public static final int CORE = 2;
    public static final float FINE = 1.0F;
    private static final double GRID = 7.0;
    private static final double TEXEL = 1.0 / 16.0;
    private static final double HALO_REACH = 1.4;
    private static final double[] LEVEL_GLOW = {1.0, 0.7, 0.22};
    private static final double[] LEVEL_CORE = {1.0, 0.8, 0.3};
    private static final Segment[] NONE = new Segment[0];
    private static final ThreadLocal<Map<Long, Segment[]>> CACHE = ThreadLocal.withInitial(() -> new LinkedHashMap<>(64, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Segment[]> eldest) {
            return this.size() > 512;
        }
    });

    private RootCrackPattern() {
    }

    public interface Sink {
        void texel(int layer, int tx, int tz, float value, float kind);
    }

    private record Segment(double ax, double az, double bx, double bz, double wa, double wb, double strength, int level, double minX, double maxX,
        double minZ, double maxZ) {
        static Segment of(double ax, double az, double bx, double bz, double wa, double wb, double strength, int level) {
            return new Segment(ax, az, bx, bz, wa, wb, strength, level, Math.min(ax, bx), Math.max(ax, bx), Math.min(az, bz), Math.max(az, bz));
        }
    }

    private static final class Random {
        private long state;

        Random(int cx, int cz) {
            this.state = (cx * 0x9E3779B97F4A7C15L) ^ (cz * 0xC2B2AE3D27D4EB4FL) ^ 0x5DEECE66DL;
        }

        double next() {
            this.state += 0x9E3779B97F4A7C15L;
            long z = this.state;
            z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
            z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
            z ^= z >>> 31;
            return (z >>> 11) * 0x1.0p-53;
        }

        double range(double min, double max) {
            return min + (max - min) * this.next();
        }
    }

    public static boolean blockMayCrack(int bx, int bz) {
        double x = bx + 0.5;
        double z = bz + 0.5;
        int cx = (int)Math.floor(x / GRID);
        int cz = (int)Math.floor(z / GRID);
        for (int dz = -2; dz <= 2; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (Segment s : seed(cx + dx, cz + dz)) {
                    if (closest(s, x, z)[0] - Math.max(s.wa(), s.wb()) < 0.75 + HALO_REACH) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void block(int bx, int bz, Sink sink) {
        int cx = (int)Math.floor((bx + 0.5) / GRID);
        int cz = (int)Math.floor((bz + 0.5) / GRID);
        double pad = HALO_REACH + 0.5;
        List<Segment> near = new ArrayList<>();
        for (int dz = -2; dz <= 2; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (Segment s : seed(cx + dx, cz + dz)) {
                    if (s.maxX() + pad >= bx && s.minX() - pad <= bx + 1 && s.maxZ() + pad >= bz && s.minZ() - pad <= bz + 1) {
                        near.add(s);
                    }
                }
            }
        }
        if (near.isEmpty()) {
            return;
        }
        for (int tz = 0; tz < 16; tz++) {
            for (int tx = 0; tx < 16; tx++) {
                double x = bx + (tx + 0.5) * TEXEL;
                double z = bz + (tz + 0.5) * TEXEL;
                double qx = x + 0.12 * (vnoise(x * 1.7, z * 1.7, 23) - 0.5);
                double qz = z + 0.12 * (vnoise(x * 1.7, z * 1.7, 24) - 0.5);
                Segment best = null;
                double bestEdge = Double.MAX_VALUE;
                double bestD = 0.0;
                double bestW = 0.0;
                double haloValue = 0.0;
                for (Segment s : near) {
                    double[] hit = closest(s, qx, qz);
                    double d = hit[0];
                    double w = s.wa() + (s.wb() - s.wa()) * hit[1];
                    double edge = d - w;
                    if (edge < bestEdge) {
                        bestEdge = edge;
                        best = s;
                        bestD = d;
                        bestW = w;
                    }
                    double taper = Math.min(1.0, w / 0.05);
                    double glow = Math.exp(-Math.max(edge, 0.0) * 2.4) * s.strength() * LEVEL_GLOW[s.level()] * (0.25 + 0.75 * taper);
                    haloValue = Math.max(haloValue, glow);
                }
                if (best == null) {
                    continue;
                }
                boolean gap = bestEdge < 0.0 && bestW > 0.012;
                boolean rim = !gap && best.level() == 0 && bestW > 0.02 && bestEdge < TEXEL;
                double halo = Math.floor(haloValue * 5.0) / 5.0 * 0.55;
                if (rim) {
                    sink.texel(RIM, tx, tz, 1.0F, 0.0F);
                }
                if (!gap && halo > 0.0) {
                    sink.texel(HALO, tx, tz, (float)halo, 0.0F);
                }
                if (gap) {
                    double inner = smoothstep(bestW, 0.0, bestD);
                    if (best.level() == 0) {
                        sink.texel(CORE, tx, tz, (float)inner, 0.0F);
                    } else {
                        sink.texel(CORE, tx, tz, (float)(LEVEL_CORE[best.level()] * (0.6 + 0.4 * inner)), FINE);
                    }
                }
            }
        }
    }

    private static Segment[] seed(int cx, int cz) {
        long key = ((long)cx << 32) ^ (cz & 0xFFFFFFFFL);
        Map<Long, Segment[]> cache = CACHE.get();
        Segment[] cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        Segment[] made = grow(cx, cz);
        cache.put(key, made);
        return made;
    }

    private static Segment[] grow(int cx, int cz) {
        double zone = smoothstep(0.3, 0.7, vnoise(cx * GRID * 0.03, cz * GRID * 0.03, 31));
        if (hash(cx, cz, 13) >= 0.04 + 0.2 * zone) {
            return NONE;
        }
        Random random = new Random(cx, cz);
        double x = (cx + 0.15 + 0.7 * random.next()) * GRID;
        double z = (cz + 0.15 + 0.7 * random.next()) * GRID;
        double strength = 0.55 + 0.45 * random.next();
        double width = 0.05 + 0.06 * strength;
        List<Segment> out = new ArrayList<>();
        double angle = random.next() * Math.PI * 2.0;
        for (int arm = 0; arm < 2; arm++) {
            double armAngle = angle + arm * Math.PI + random.range(-0.45, 0.45);
            int count = arm == 0 ? 4 + (int)(random.next() * 3.0) : 2 + (int)(random.next() * 4.0);
            branch(out, random, x, z, armAngle, count, 0.85, 1.3, width, strength, 0);
        }
        return out.toArray(NONE);
    }

    private static void branch(List<Segment> out, Random random, double x, double z, double angle, int count, double minLength, double maxLength,
        double width, double strength, int level) {
        for (int i = 0; i < count; i++) {
            double w0 = width * Math.pow(1.0 - i / (double)count, 0.75);
            double w1 = width * Math.pow(1.0 - (i + 1) / (double)count, 0.75);
            angle += random.range(-0.6, 0.6);
            double length = random.range(minLength, maxLength);
            double nx = x + Math.cos(angle) * length;
            double nz = z + Math.sin(angle) * length;
            out.add(Segment.of(x, z, nx, nz, w0, w1, strength, level));
            if (level < 2 && i > 0 && random.next() < (level == 0 ? 0.75 : 0.5)) {
                double side = random.next() < 0.5 ? -1.0 : 1.0;
                double branchAngle = angle + side * (level == 0 ? random.range(0.45, 0.9) : random.range(0.6, 1.2));
                int branchCount = level == 0 ? 3 + (int)(random.next() * 3.0) : 2 + (int)(random.next() * 2.0);
                double branchWidth = w0 * (level == 0 ? 0.75 : 0.5);
                branch(out, random, x, z, branchAngle, branchCount, minLength * (level == 0 ? 0.75 : 0.55), maxLength * (level == 0 ? 0.8 : 0.6), branchWidth,
                    strength, level + 1);
            }
            if (level == 0 && i == count - 2 && random.next() < 0.45) {
                double fork = random.range(0.35, 0.6);
                branch(out, random, nx, nz, angle + fork, 2, minLength * 0.7, maxLength * 0.75, w1 * 1.1, strength, 1);
                branch(out, random, nx, nz, angle - fork, 2, minLength * 0.7, maxLength * 0.75, w1 * 1.1, strength, 1);
                return;
            }
            x = nx;
            z = nz;
        }
    }

    private static double[] closest(Segment s, double x, double z) {
        double dx = s.bx() - s.ax();
        double dz = s.bz() - s.az();
        double lengthSq = dx * dx + dz * dz;
        double t = lengthSq < 1.0E-9 ? 0.0 : Math.max(0.0, Math.min(1.0, ((x - s.ax()) * dx + (z - s.az()) * dz) / lengthSq));
        double px = s.ax() + dx * t - x;
        double pz = s.az() + dz * t - z;
        return new double[] {Math.sqrt(px * px + pz * pz), t};
    }

    static double hash(int x, int y, int seed) {
        int h = x * 0x8da6b343 ^ y * 0xd8163841 ^ seed * 0xcb1ab31f;
        h ^= h >>> 16;
        h *= 0x7feb352d;
        h ^= h >>> 15;
        h *= 0x846ca68b;
        h ^= h >>> 16;
        return (h & 0xFFFFFFFFL) / 4294967295.0;
    }

    static double vnoise(double px, double py, int seed) {
        int ix = (int)Math.floor(px);
        int iy = (int)Math.floor(py);
        double fx = px - ix;
        double fy = py - iy;
        double ux = fx * fx * (3.0 - 2.0 * fx);
        double uy = fy * fy * (3.0 - 2.0 * fy);
        double a = hash(ix, iy, seed) + (hash(ix + 1, iy, seed) - hash(ix, iy, seed)) * ux;
        double b = hash(ix, iy + 1, seed) + (hash(ix + 1, iy + 1, seed) - hash(ix, iy + 1, seed)) * ux;
        return a + (b - a) * uy;
    }

    private static double smoothstep(double edge0, double edge1, double x) {
        double t = Math.max(0.0, Math.min(1.0, (x - edge0) / (edge1 - edge0)));
        return t * t * (3.0 - 2.0 * t);
    }
}
