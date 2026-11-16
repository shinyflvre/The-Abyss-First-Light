package net.yezon.theabyss.world.structures;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public final class GiantArchShape {
    public static final int LEG_DEPTH = 24;
    public static final double WARP = 7.0;
    public static final double LUMP = 3.0;
    public static final double MARGIN = WARP + LUMP + 3.0;
    private static final double STEP = 0.75;

    public final long seed;
    public final int ax;
    public final int ay;
    public final int az;
    public final int bx;
    public final int by;
    public final int bz;
    public final float rise;
    public final float rTop;
    public final float rLeg;
    public final float widthRatio;
    public final float bow;
    public final float gamma;
    public final float power;
    public final float legA;
    public final float legB;
    public final float twist;
    public final float period;

    private final double sideX;
    private final double sideZ;
    private float[] px;
    private float[] py;
    private float[] pz;
    private float[] nx;
    private float[] ny;
    private float[] nz;
    private float[] radius;
    private float[] param;
    private int count;
    private SimplexNoise warp;
    private SimplexNoise lump;
    private SimplexNoise strata;
    private SimplexNoise vein;

    public GiantArchShape(long seed, int ax, int ay, int az, int bx, int by, int bz, float rise, float rTop, float rLeg, float widthRatio, float bow,
        float gamma, float power, float legA, float legB, float twist, float period) {
        this.seed = seed;
        this.ax = ax;
        this.ay = ay;
        this.az = az;
        this.bx = bx;
        this.by = by;
        this.bz = bz;
        this.rise = rise;
        this.rTop = rTop;
        this.rLeg = rLeg;
        this.widthRatio = widthRatio;
        this.bow = bow;
        this.gamma = gamma;
        this.power = power;
        this.legA = legA;
        this.legB = legB;
        this.twist = twist;
        this.period = period;
        double dx = bx - ax;
        double dz = bz - az;
        double length = Math.max(1.0, Math.sqrt(dx * dx + dz * dz));
        this.sideX = -dz / length;
        this.sideZ = dx / length;
    }

    public static GiantArchShape load(CompoundTag tag) {
        return new GiantArchShape(tag.getLongOr("Seed", 0L), tag.getIntOr("AX", 0), tag.getIntOr("AY", 0), tag.getIntOr("AZ", 0), tag.getIntOr("BX", 0),
            tag.getIntOr("BY", 0), tag.getIntOr("BZ", 0), tag.getFloatOr("Rise", 40.0F), tag.getFloatOr("RTop", 6.0F), tag.getFloatOr("RLeg", 14.0F),
            tag.getFloatOr("Width", 1.4F), tag.getFloatOr("Bow", 0.0F), tag.getFloatOr("Gamma", 1.0F), tag.getFloatOr("Power", 2.0F),
            tag.getFloatOr("LegA", 1.0F), tag.getFloatOr("LegB", 1.0F), tag.getFloatOr("Twist", 0.0F), tag.getFloatOr("Period", 7.0F));
    }

    public void save(CompoundTag tag) {
        tag.putLong("Seed", this.seed);
        tag.putInt("AX", this.ax);
        tag.putInt("AY", this.ay);
        tag.putInt("AZ", this.az);
        tag.putInt("BX", this.bx);
        tag.putInt("BY", this.by);
        tag.putInt("BZ", this.bz);
        tag.putFloat("Rise", this.rise);
        tag.putFloat("RTop", this.rTop);
        tag.putFloat("RLeg", this.rLeg);
        tag.putFloat("Width", this.widthRatio);
        tag.putFloat("Bow", this.bow);
        tag.putFloat("Gamma", this.gamma);
        tag.putFloat("Power", this.power);
        tag.putFloat("LegA", this.legA);
        tag.putFloat("LegB", this.legB);
        tag.putFloat("Twist", this.twist);
        tag.putFloat("Period", this.period);
    }

    public double curveX(double t) {
        return this.ax + (this.bx - this.ax) * t + this.sideX * this.bow * Math.sin(Math.PI * t);
    }

    public double curveZ(double t) {
        return this.az + (this.bz - this.az) * t + this.sideZ * this.bow * Math.sin(Math.PI * t);
    }

    public double curveY(double t) {
        double u = Math.pow(Mth.clamp(t, 0.0, 1.0), this.gamma);
        double f = Math.pow(Math.max(0.0, 1.0 - Math.pow(Math.abs(2.0 * u - 1.0), this.power)), 1.0 / this.power);
        return Mth.lerp(t, this.ay, this.by) + this.rise * f;
    }

    public double radius(double t, double y) {
        double base = this.rTop + (this.rLeg - this.rTop) * Math.pow(Math.abs(2.0 * t - 1.0), 1.6);
        double foot = t < 0.5 ? this.ay : this.by;
        double flare = 1.0 + 0.4 * Mth.clamp((foot + 10.0 - y) / 10.0, 0.0, 1.0);
        return base * Mth.lerp(t, this.legA, this.legB) * flare;
    }

    public double reach(int i) {
        return this.radius[i] * Math.max(1.0, this.widthRatio) + MARGIN;
    }

    public synchronized void build() {
        if (this.px != null) {
            return;
        }
        int dense = 4000;
        double[] dx = new double[dense + 1];
        double[] dy = new double[dense + 1];
        double[] dz = new double[dense + 1];
        double[] arc = new double[dense + 1];
        for (int i = 0; i <= dense; i++) {
            double t = i / (double)dense;
            dx[i] = curveX(t);
            dy[i] = curveY(t);
            dz[i] = curveZ(t);
            if (i > 0) {
                double ex = dx[i] - dx[i - 1];
                double ey = dy[i] - dy[i - 1];
                double ez = dz[i] - dz[i - 1];
                arc[i] = arc[i - 1] + Math.sqrt(ex * ex + ey * ey + ez * ez);
            }
        }
        int legSteps = (int)(LEG_DEPTH / STEP);
        int curveSteps = Math.max(2, (int)(arc[dense] / STEP));
        int total = legSteps * 2 + curveSteps + 1;
        float[] sx = new float[total];
        float[] sy = new float[total];
        float[] sz = new float[total];
        float[] st = new float[total];
        int n = 0;
        for (int i = legSteps; i > 0; i--) {
            sx[n] = this.ax;
            sy[n] = (float)(this.ay - i * STEP);
            sz[n] = this.az;
            st[n] = 0.0F;
            n++;
        }
        int j = 0;
        for (int i = 0; i <= curveSteps; i++) {
            double target = arc[dense] * i / curveSteps;
            while (j < dense - 1 && arc[j + 1] < target) {
                j++;
            }
            double span = arc[j + 1] - arc[j];
            double f = span <= 0.0 ? 0.0 : (target - arc[j]) / span;
            sx[n] = (float)Mth.lerp(f, dx[j], dx[j + 1]);
            sy[n] = (float)Mth.lerp(f, dy[j], dy[j + 1]);
            sz[n] = (float)Mth.lerp(f, dz[j], dz[j + 1]);
            st[n] = (float)((j + f) / dense);
            n++;
        }
        for (int i = 1; i <= legSteps; i++) {
            sx[n] = this.bx;
            sy[n] = (float)(this.by - i * STEP);
            sz[n] = this.bz;
            st[n] = 1.0F;
            n++;
        }
        float[] nxs = new float[n];
        float[] nys = new float[n];
        float[] nzs = new float[n];
        float[] rs = new float[n];
        for (int i = 0; i < n; i++) {
            int a = Math.max(0, i - 1);
            int b = Math.min(n - 1, i + 1);
            double tx = sx[b] - sx[a];
            double ty = sy[b] - sy[a];
            double tz = sz[b] - sz[a];
            double tl = Math.sqrt(tx * tx + ty * ty + tz * tz);
            if (tl < 1.0E-6) {
                tx = 0.0;
                ty = 1.0;
                tz = 0.0;
            } else {
                tx /= tl;
                ty /= tl;
                tz /= tl;
            }
            double cx = this.sideZ * ty;
            double cy = this.sideX * tz - this.sideZ * tx;
            double cz = -this.sideX * ty;
            double cl = Math.sqrt(cx * cx + cy * cy + cz * cz);
            if (cl < 1.0E-6) {
                cx = 0.0;
                cy = 1.0;
                cz = 0.0;
                cl = 1.0;
            }
            nxs[i] = (float)(cx / cl);
            nys[i] = (float)(cy / cl);
            nzs[i] = (float)(cz / cl);
            rs[i] = (float)radius(st[i], sy[i]);
            if (i < legSteps || i >= n - legSteps) {
                rs[i] = (float)(Mth.lerp(st[i], this.legA, this.legB) * this.rLeg * 1.4);
            }
        }
        LegacyRandomSource random = new LegacyRandomSource(this.seed);
        this.warp = new SimplexNoise(random);
        this.lump = new SimplexNoise(random);
        this.strata = new SimplexNoise(random);
        this.vein = new SimplexNoise(random);
        this.py = sy;
        this.pz = sz;
        this.nx = nxs;
        this.ny = nys;
        this.nz = nzs;
        this.radius = rs;
        this.param = st;
        this.count = n;
        this.px = sx;
    }

    public BoundingBox bounds(int minWorldY) {
        build();
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;
        for (int i = 0; i < this.count; i++) {
            double r = this.radius[i];
            double w = r * Math.max(1.0, this.widthRatio);
            double rx = w * Math.abs(this.sideX) + r * Math.abs(this.sideZ) + MARGIN;
            double rz = w * Math.abs(this.sideZ) + r * Math.abs(this.sideX) + MARGIN;
            double ry = r + MARGIN;
            minX = Math.min(minX, this.px[i] - rx);
            minY = Math.min(minY, this.py[i] - ry);
            minZ = Math.min(minZ, this.pz[i] - rz);
            maxX = Math.max(maxX, this.px[i] + rx);
            maxY = Math.max(maxY, this.py[i] + ry);
            maxZ = Math.max(maxZ, this.pz[i] + rz);
        }
        return new BoundingBox(Mth.floor(minX), Math.max(minWorldY, Mth.floor(minY) - 64), Mth.floor(minZ), Mth.ceil(maxX), Mth.ceil(maxY) + 2,
            Mth.ceil(maxZ));
    }

    public int count() {
        return this.count;
    }

    public float sampleX(int i) {
        return this.px[i];
    }

    public float sampleY(int i) {
        return this.py[i];
    }

    public float sampleZ(int i) {
        return this.pz[i];
    }

    public double topY() {
        double top = -Double.MAX_VALUE;
        for (int i = 0; i < this.count; i++) {
            top = Math.max(top, this.py[i]);
        }
        return top;
    }

    public double band(double x, double y, double z) {
        return (y + 4.0 * this.strata.get(x * 0.02, z * 0.02)) / this.period;
    }

    public boolean vein(double x, double y, double z) {
        return Math.abs(this.vein.get(x * 0.045, y * 0.045, z * 0.045)) < 0.018;
    }

    public double distance(double x, double y, double z, int[] candidates, int candidateCount) {
        double qx = x + WARP * this.warp.get(x * 0.013, y * 0.013, z * 0.013);
        double qy = y + WARP * 0.6 * this.warp.get(x * 0.013 + 31.7, y * 0.013, z * 0.013);
        double qz = z + WARP * this.warp.get(x * 0.013, y * 0.013 + 71.3, z * 0.013);
        int best = -1;
        double bestSq = Double.MAX_VALUE;
        for (int c = 0; c < candidateCount; c++) {
            int i = candidates[c];
            double ex = qx - this.px[i];
            double ey = qy - this.py[i];
            double ez = qz - this.pz[i];
            double sq = ex * ex + ey * ey + ez * ez;
            if (sq < bestSq) {
                bestSq = sq;
                best = i;
            }
        }
        if (best < 0) {
            return Double.MAX_VALUE;
        }
        double ex = qx - this.px[best];
        double ey = qy - this.py[best];
        double ez = qz - this.pz[best];
        double u = ex * this.sideX + ez * this.sideZ;
        double v = ex * this.nx[best] + ey * this.ny[best] + ez * this.nz[best];
        double angle = this.twist * (this.param[best] - 0.5) * 2.0;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double ru = u * cos - v * sin;
        double rv = u * sin + v * cos;
        double w = this.radius[best] * this.widthRatio;
        double h = this.radius[best];
        double e = Math.sqrt(ru * ru / (w * w) + rv * rv / (h * h));
        double dist = (e - 1.0) * Math.min(w, h);
        double amount = Math.min(1.0, h / 10.0);
        dist -= amount * (LUMP * this.lump.get(qx * 0.035, qy * 0.035, qz * 0.035) + 1.2 * this.lump.get(qx * 0.11 + 17.0, qy * 0.11, qz * 0.11));
        double band = band(x, y, z);
        dist -= amount * 1.2 * (band - Math.floor(band) - 0.5);
        return dist;
    }
}
