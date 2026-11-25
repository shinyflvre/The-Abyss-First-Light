package net.yezon.theabyss.client.fx.spell;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.fx.MagicLights;
import net.yezon.theabyss.client.renderer.LightTrail;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.InvertedSky;
import net.yezon.theabyss.ring.fx.FxOptions;

final class InvertedSkyFx extends Effect {
    static final RenderType DEEP = RenderTypes.entityCutout(texture("sky_deep"));
    static final RenderType MOON = RenderTypes.entityCutout(texture("sky_moon"));
    static final RenderType[] STARS = frames("sky_stars_", 24, false);
    static final RenderType[] AURORA = frames("sky_aurora_", 24, true);
    static final RenderType RIM = RenderTypes.entityCutout(texture("hole_rim"));
    static final RenderType CRACK = RenderTypes.entityTranslucentEmissive(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "textures/fx/circle/crack.png"));
    private static final int LAYER_FRAMES = 24;
    private static final double SPAN = 192.0 / 2.0 / 80.0;
    private static final double DEPTH = 0.5;
    private static final int RIM_GRID = 12;
    private static final int CURTAIN_SEGMENTS = 96;
    private static final double[] CURTAIN_EDGE = {1.087, 1.090, 1.083, 1.085, 1.082, 1.067, 1.081, 1.085, 1.092, 1.058, 1.031, 1.016, 1.021, 1.042, 1.089, 1.106, 1.107, 1.082, 1.082, 1.092, 1.099, 1.102, 1.104, 1.105, 1.105, 1.100, 1.114, 1.134, 1.133, 1.119, 1.102, 1.099, 1.091, 1.079, 1.069, 1.066, 1.065, 1.067, 1.059, 1.051, 1.038, 1.027, 1.013, 1.007, 1.012, 1.026, 1.061, 1.072, 1.074, 1.060, 1.054, 1.039, 1.043, 1.031, 1.037, 1.027, 1.047, 1.067, 1.086, 1.105, 1.122, 1.129, 1.125, 1.121, 1.130, 1.140, 1.138, 1.142, 1.159, 1.157, 1.138, 1.107, 1.114, 1.141, 1.150, 1.136, 1.103, 1.101, 1.111, 1.143, 1.132, 1.120, 1.083, 1.118, 1.139, 1.138, 1.098, 1.069, 1.063, 1.056, 1.056, 1.066, 1.078, 1.093, 1.090, 1.096};
    private static final double CURTAIN_TILE = 2.0;
    private static final double CURTAIN_HEIGHT = 4.0;
    private static final float AFTER = 30.0F;
    private static final int RINGS = 6;
    private static final int SEGMENTS = 96;
    private static final double[] EDGE = {1.069, 1.034, 1.041, 1.039, 1.055, 1.036, 1.007, 1.094, 1.056, 1.029, 0.993, 0.974, 0.979, 1.011, 1.034, 1.119, 1.046, 1.039, 1.049, 1.056, 1.055, 1.065, 1.066, 1.071, 1.069, 1.071, 1.066, 1.116, 1.118, 1.062, 1.062, 1.066, 1.057, 1.041, 1.031, 1.021, 1.032, 1.030, 1.031, 1.019, 1.018, 0.996, 0.981, 0.970, 0.965, 1.001, 1.015, 1.071, 1.032, 1.009, 1.028, 1.014, 0.978, 1.036, 0.968, 0.982, 1.002, 1.029, 1.046, 1.069, 1.085, 1.104, 1.094, 1.074, 1.081, 1.107, 1.103, 1.089, 1.120, 1.180, 1.079, 1.059, 1.057, 1.084, 1.180, 1.090, 1.055, 1.062, 1.089, 1.081, 1.156, 1.059, 1.054, 1.038, 1.156, 1.104, 1.034, 1.044, 1.031, 1.010, 1.020, 1.036, 1.055, 1.052, 1.066, 1.034};
    private static final int RISERS = 200;

    private final Vec3 centre;
    private final double radius;
    private final List<Riser> risers = new ArrayList<>();
    private final java.util.Map<net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState> hidden = new java.util.HashMap<>();
    private boolean cleared;
    private double[][] heights;
    private int size;
    private int originX;
    private int originZ;
    private boolean restored;

    InvertedSkyFx(SpellFxMessage m) {
        this.centre = SpellFx.vec(m, 0);
        this.radius = m.data().size() > 3 ? m.data().get(3) : InvertedSky.RADIUS;
    }

    private static RenderType[] frames(String prefix, int count, boolean translucent) {
        RenderType[] out = new RenderType[count];
        for (int i = 0; i < count; i++) {
            Identifier id = texture(prefix + i);
            out[i] = translucent ? RenderTypes.entityTranslucent(id) : RenderTypes.entityCutout(id);
        }
        return out;
    }

    static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "textures/fx/inverted_sky/" + name + ".png");
    }

    @Override
    public float life() {
        return InvertedSky.END + AFTER;
    }

    private static float open(float age) {
        if (age < InvertedSky.OPEN) {
            return (float)smooth(age / InvertedSky.OPEN);
        }
        if (age < InvertedSky.CLOSE) {
            return 1.0F;
        }
        return 1.0F - (float)smooth((age - InvertedSky.CLOSE) / (double)(InvertedSky.END - InvertedSky.CLOSE));
    }

    private double edge(double angle, float o) {
        return this.radius * o;
    }

    @Override
    public void draw(Frame f) {
        float age = this.age();
        float o = open(age);
        if (age < InvertedSky.END && o > 0.002F) {
            this.drawHole(f, age, o);
            this.drawCurtain(f, age, o);
        }
        if (age < 34.0F) {
            float k = (float)smooth(age / 6.0) * Math.max(0.0F, 1.0F - Math.max(0.0F, age - 14.0F) / 20.0F);
            this.crack(f, this.radius * 1.5 * (0.4 + 0.6 * smooth(age / 8.0)), 0x3FE6F0, k, 0.0);
        }
        if (age >= InvertedSky.CLOSE && age < InvertedSky.END + AFTER) {
            float k = Math.min(1.0F, (age - InvertedSky.CLOSE) / 4.0F) * Math.max(0.0F, 1.0F - (age - InvertedSky.END) / AFTER);
            this.crack(f, this.radius * 1.25, 0x1FA8B8, k * 0.8F, 1.3);
        }
        if (age >= InvertedSky.CLOSE && age < InvertedSky.END + 4.0F) {
            float k = Math.min(1.0F, (InvertedSky.END + 4.0F - age) / 6.0F);
            float seamO = Math.max(0.03F, open(Math.min(age, InvertedSky.END - 0.01F)));
            Vec3[] p = new Vec3[SEGMENTS + 1];
            for (int s = 0; s <= SEGMENTS; s++) {
                double a = s * Math.PI * 2.0 / SEGMENTS;
                double r = this.radius * seamO;
                p[s] = this.centre.add(Math.cos(a) * r, 0.14, Math.sin(a) * r);
            }
            boolean fresh = age < InvertedSky.CLOSE + 3.0F;
            f.ribbon(p, 0.45F, k, fresh ? 0xEAFFFF : 0x3FE6F0);
            f.ribbon(p, 0.16F, k, 0xEAFFFF);
            if (age > InvertedSky.END - 3.0F) {
                float flash = Math.max(0.0F, 1.0F - (age - (InvertedSky.END - 3.0F)) / 7.0F);
                f.flare(this.centre.add(0.0, 0.6, 0.0), 6.0F * flash, InvertedSky.CYAN, flash);
                f.flare(this.centre.add(0.0, 0.6, 0.0), 2.5F * flash, 0xFFFFFF, flash);
            }
        }
        for (Riser s : this.risers) {
            float u = Math.min(1.0F, (s.age + f.pt) / s.life);
            float head = (1.0F - (1.0F - u) * (1.0F - u)) * s.height;
            float tail = Math.max(0.0F, head - s.height * 0.5F);
            float a = Math.min(1.0F, u * 6.0F) * Math.min(1.0F, (1.0F - u) * 3.0F);
            Vec3 top = s.base.add(0.0, head, 0.0);
            Vec3 low = s.base.add(0.0, tail, 0.0);
            f.ribbon(new Vec3[] {low, low.lerp(top, 0.6), top}, new float[] {0.012F, 0.05F, 0.07F}, new float[] {0.0F, a * 0.55F, a}, s.rgb);
            f.flare(top, 0.2F, s.rgb, a * 0.85F);
        }
        if (o > 0.01F && age < InvertedSky.END) {
            MagicLights.add(this.centre.add(0.0, 3.0, 0.0), 0x12A9B8, (float)(this.radius * 1.6), 0.3F * o);
            MagicLights.add(this.centre.add(0.0, 2.0, 0.0), 0x8D45DE, (float)this.radius, 0.12F * o);
        }
    }

    private void drawHole(Frame f, float age, float o) {
        Vec3 cam = f.camera;
        double r = this.radius * o;
        int frame = (int)(age / 4.0F) % LAYER_FRAMES;
        int alpha = (int)(255.0F * Mth.clamp(o * 1.4F, 0.0F, 1.0F));
        double[][] ground = new double[RINGS + 1][SEGMENTS + 1];
        for (int k = 0; k <= RINGS; k++) {
            for (int s = 0; s <= SEGMENTS; s++) {
                double a = s * Math.PI * 2.0 / SEGMENTS;
                double t = k / (double)RINGS * EDGE[s % EDGE.length];
                ground[k][s] = this.ground(f, this.centre.x + Math.cos(a) * r * t, this.centre.z + Math.sin(a) * r * t);
            }
        }
        this.layer(f, DEEP, cam, r, ground, 0.03, 1.0, 0xFFFFFFFF);
        this.layer(f, MOON, cam, r, ground, 0.045, 0.85, 0xFFFFFFFF);
        this.layer(f, STARS[frame], cam, r, ground, 0.06, 0.7, 0xFFFFFFFF);
        this.layer(f, AURORA[frame], cam, r, ground, 0.075, 0.45, alpha << 24 | 0xFFFFFF);
        double half = r * SPAN;
        int n = RIM_GRID;
        Vec3[][] rp = new Vec3[n + 1][n + 1];
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= n; j++) {
                double x = this.centre.x - half + 2.0 * half * i / n;
                double z = this.centre.z - half + 2.0 * half * j / n;
                rp[i][j] = f.rel(new Vec3(x, this.ground(f, x, z) + 0.1, z));
            }
        }
        f.mesh(RIM, (pose, buffer, cc) -> {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    float u0 = i / (float)n;
                    float u1 = (i + 1) / (float)n;
                    float v0 = j / (float)n;
                    float v1 = (j + 1) / (float)n;
                    Frame.vertex(buffer, pose, rp[i][j], u0, v0, 0xFFFFFFFF);
                    Frame.vertex(buffer, pose, rp[i][j + 1], u0, v1, 0xFFFFFFFF);
                    Frame.vertex(buffer, pose, rp[i + 1][j + 1], u1, v1, 0xFFFFFFFF);
                    Frame.vertex(buffer, pose, rp[i + 1][j], u1, v0, 0xFFFFFFFF);
                }
            }
        });
    }

    private void crack(Frame f, double half, int rgb, float light, double turn) {
        if (light <= 0.01F) {
            return;
        }
        int n = RIM_GRID;
        Vec3[][] p = new Vec3[n + 1][n + 1];
        double cos = Math.cos(turn);
        double sin = Math.sin(turn);
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= n; j++) {
                double lx = -half + 2.0 * half * i / n;
                double lz = -half + 2.0 * half * j / n;
                double x = this.centre.x + lx * cos - lz * sin;
                double z = this.centre.z + lx * sin + lz * cos;
                p[i][j] = f.rel(new Vec3(x, this.ground(f, x, z) + 0.13, z));
            }
        }
        int argb = Math.round(255.0F * Math.min(1.0F, light)) << 24 | rgb & 0xFFFFFF;
        f.mesh(CRACK, (pose, buffer, c) -> {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    float u0 = i / (float)n;
                    float u1 = (i + 1) / (float)n;
                    float v0 = j / (float)n;
                    float v1 = (j + 1) / (float)n;
                    Frame.vertex(buffer, pose, p[i][j], u0, v0, argb);
                    Frame.vertex(buffer, pose, p[i][j + 1], u0, v1, argb);
                    Frame.vertex(buffer, pose, p[i + 1][j + 1], u1, v1, argb);
                    Frame.vertex(buffer, pose, p[i + 1][j], u1, v0, argb);
                }
            }
        });
    }

    private void drawCurtain(Frame f, float age, float o) {
        int n = CURTAIN_SEGMENTS;
        Vec3[] top = new Vec3[n + 1];
        Vec3[] bottom = new Vec3[n + 1];
        float[] light = new float[n + 1];
        float[] u = new float[n + 1];
        double rise = smooth((age - 3.0) / 10.0) * o;
        if (rise <= 0.01) {
            return;
        }
        double run = 0.0;
        Vec3 last = null;
        for (int s = 0; s <= n; s++) {
            double a = s * Math.PI * 2.0 / n;
            double r = this.radius * o * CURTAIN_EDGE[s % CURTAIN_EDGE.length];
            double x = this.centre.x + Math.cos(a) * r;
            double z = this.centre.z + Math.sin(a) * r;
            double h = CURTAIN_HEIGHT * rise * (0.8 + 0.2 * Math.sin(a * 5.0 + age * 0.15));
            Vec3 base = new Vec3(x, this.ground(f, x, z) + 0.12, z);
            if (last != null) {
                run += base.distanceTo(last);
            }
            last = base;
            u[s] = (float)(run / CURTAIN_TILE);
            bottom[s] = f.rel(base);
            top[s] = f.rel(base.add(0.0, h, 0.0));
            light[s] = (float)rise * (0.75F + 0.25F * (float)Math.sin(a * 4.0 + age * 0.3));
        }
        AuroraCageFx.glow(f, bottom, top, light);
        f.mesh(AuroraCageFx.CURTAIN[(int)(age / 3.0F) % AuroraCageFx.CURTAIN.length], (pose, buffer, c) -> {
            for (int s = 0; s < n; s++) {
                int c0 = shade(light[s]);
                int c1 = shade(light[s + 1]);
                Frame.vertex(buffer, pose, top[s], u[s], 0.0F, c0);
                Frame.vertex(buffer, pose, bottom[s], u[s], 1.0F, c0);
                Frame.vertex(buffer, pose, bottom[s + 1], u[s + 1], 1.0F, c1);
                Frame.vertex(buffer, pose, top[s + 1], u[s + 1], 0.0F, c1);
            }
        });
    }

    private static int shade(float light) {
        int v = Math.round(255.0F * Mth.clamp(light, 0.0F, 1.0F));
        return 0xFF << 24 | v << 16 | v << 8 | v;
    }

    private double ground(Frame f, double x, double z) {
        if (this.heights == null) {
            this.bake(f.level);
        }
        double gx = x - this.originX - 0.5;
        double gz = z - this.originZ - 0.5;
        int x0 = Mth.clamp(Mth.floor(gx), 0, this.size - 2);
        int z0 = Mth.clamp(Mth.floor(gz), 0, this.size - 2);
        double tx = Mth.clamp(gx - x0, 0.0, 1.0);
        double tz = Mth.clamp(gz - z0, 0.0, 1.0);
        double a = this.heights[x0][z0] * (1.0 - tx) + this.heights[x0 + 1][z0] * tx;
        double b = this.heights[x0][z0 + 1] * (1.0 - tx) + this.heights[x0 + 1][z0 + 1] * tx;
        return a * (1.0 - tz) + b * tz;
    }

    private void bake(ClientLevel level) {
        int reach = (int)Math.ceil(this.radius * SPAN) + 3;
        this.size = reach * 2 + 1;
        this.originX = Mth.floor(this.centre.x) - reach;
        this.originZ = Mth.floor(this.centre.z) - reach;
        double[][] raw = new double[this.size][this.size];
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                int top = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.originX + i, this.originZ + j);
                raw[i][j] = Mth.clamp(top, this.centre.y - 1.5, this.centre.y + 1.5);
            }
        }
        this.heights = new double[this.size][this.size];
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                double sum = 0.0;
                int n = 0;
                for (int di = -2; di <= 2; di++) {
                    for (int dj = -2; dj <= 2; dj++) {
                        int a = i + di;
                        int b = j + dj;
                        if (a >= 0 && b >= 0 && a < this.size && b < this.size) {
                            sum += raw[a][b];
                            n++;
                        }
                    }
                }
                this.heights[i][j] = sum / n;
            }
        }
    }

    private void layer(Frame f, RenderType type, Vec3 cam, double r, double[][] ground, double lift, double depth, int argb) {
        Vec3[][] p = new Vec3[RINGS + 1][SEGMENTS + 1];
        float[][] u = new float[RINGS + 1][SEGMENTS + 1];
        float[][] v = new float[RINGS + 1][SEGMENTS + 1];
        double scale = 0.5 / (SPAN * Math.max(0.05, r));
        double max = r * (SPAN - 1.0) * depth;
        for (int k = 0; k <= RINGS; k++) {
            double t = k / (double)RINGS;
            for (int s = 0; s <= SEGMENTS; s++) {
                double a = s * Math.PI * 2.0 / SEGMENTS;
                double dx = Math.cos(a) * r * t * EDGE[s % EDGE.length];
                double dz = Math.sin(a) * r * t * EDGE[s % EDGE.length];
                Vec3 at = new Vec3(this.centre.x + dx, ground[k][s] + lift, this.centre.z + dz);
                p[k][s] = f.rel(at);
                double h = Math.max(0.6, cam.y - at.y);
                double sx = (at.x - cam.x) * DEPTH * depth / h;
                double sz = (at.z - cam.z) * DEPTH * depth / h;
                double len = Math.sqrt(sx * sx + sz * sz);
                if (len > max) {
                    sx *= max / len;
                    sz *= max / len;
                }
                u[k][s] = (float)(0.5 + (dx + sx) * scale);
                v[k][s] = (float)(0.5 + (dz + sz) * scale);
            }
        }
        f.mesh(type, (pose, buffer, c) -> grid(pose, buffer, p, u, v, argb));
    }

    private static void grid(com.mojang.blaze3d.vertex.PoseStack.Pose pose, com.mojang.blaze3d.vertex.VertexConsumer buffer, Vec3[][] p, float[][] u, float[][] v, int argb) {
        for (int k = 0; k < RINGS; k++) {
            for (int s = 0; s < SEGMENTS; s++) {
                Frame.vertex(buffer, pose, p[k][s], u[k][s], v[k][s], argb);
                Frame.vertex(buffer, pose, p[k + 1][s], u[k + 1][s], v[k + 1][s], argb);
                Frame.vertex(buffer, pose, p[k + 1][s + 1], u[k + 1][s + 1], v[k + 1][s + 1], argb);
                Frame.vertex(buffer, pose, p[k][s + 1], u[k][s + 1], v[k][s + 1], argb);
            }
        }
    }

    private void hidePlants(ClientLevel level) {
        int r = (int)Math.ceil(this.radius * 1.05);
        int cx = Mth.floor(this.centre.x);
        int cz = Mth.floor(this.centre.z);
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                if (x * x + z * z > this.radius * this.radius * 1.1) {
                    continue;
                }
                int top = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx + x, cz + z);
                for (int y = top; y <= top + 1; y++) {
                    net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(cx + x, y, cz + z);
                    net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
                    if (!state.isAir() && state.canBeReplaced() && state.getFluidState().isEmpty() && !state.hasBlockEntity()) {
                        this.hidden.put(pos, state);
                        level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private void restorePlants(ClientLevel level) {
        for (java.util.Map.Entry<net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState> e : this.hidden.entrySet()) {
            if (level.getBlockState(e.getKey()).isAir()) {
                level.setBlock(e.getKey(), e.getValue(), 2);
            }
        }
        this.hidden.clear();
    }

    @Override
    public void tick(ClientLevel level) {
        float age = this.age();
        RandomSource random = level.getRandom();
        if (!this.cleared && age >= 2.0F) {
            this.cleared = true;
            this.hidePlants(level);
        }
        if (this.cleared && !this.restored && age >= InvertedSky.END - 2.0F) {
            this.restored = true;
            this.restorePlants(level);
        }
        this.risers.removeIf(s -> ++s.age >= s.life);
        float o = open(age);
        if (age >= 3.0F && age < InvertedSky.END && o > 0.05F) {
            for (int k = 0; k < 3 && this.risers.size() < RISERS; k++) {
                double a = random.nextDouble() * Math.PI * 2.0;
                double d = Math.sqrt(random.nextDouble()) * this.edge(a, o);
                Riser s = new Riser();
                s.base = this.centre.add(Math.cos(a) * d, 0.12, Math.sin(a) * d);
                s.rgb = k == 0 ? InvertedSky.AURORA : k == 1 ? InvertedSky.CYAN : InvertedSky.VIOLET;
                s.height = 1.5F + random.nextFloat() * 2.8F;
                s.life = 12 + random.nextInt(14);
                this.risers.add(s);
            }
            for (int k = 0; k < 2; k++) {
                double a = random.nextDouble() * Math.PI * 2.0;
                double d = Math.sqrt(random.nextDouble()) * this.edge(a, o) * 0.95;
                Vec3 at = this.centre.add(Math.cos(a) * d, 0.8 + random.nextDouble() * 6.0, Math.sin(a) * d);
                emit(level, mote(0.16F + random.nextFloat() * 0.1F, 30), at, new Vec3(0.0, 0.02 + random.nextDouble() * 0.02, 0.0));
            }
            if (random.nextFloat() < 0.8F) {
                double a = random.nextDouble() * Math.PI * 2.0;
                double d = Math.sqrt(random.nextDouble()) * this.edge(a, o) * 0.9;
                emit(level, star(0.18F + random.nextFloat() * 0.1F, 34), this.centre.add(Math.cos(a) * d, 0.2, Math.sin(a) * d),
                    new Vec3(0.0, 0.035 + random.nextDouble() * 0.03, 0.0));
            }
            if (random.nextInt(3) == 0) {
                double a = random.nextDouble() * Math.PI * 2.0;
                double r = this.edge(a, o);
                emit(level, sparkle(InvertedSky.PALE, 0.11F, 18).flags(FxOptions.FLICKER | FxOptions.HOT),
                    this.centre.add(Math.cos(a) * r, 0.2, Math.sin(a) * r), new Vec3(0.0, 0.05, 0.0));
            }
        }
        if (age >= InvertedSky.END - 1.0F && age < InvertedSky.END) {
            for (int k = 0; k < 24; k++) {
                double a = random.nextDouble() * Math.PI * 2.0;
                double s = 0.15 + random.nextDouble() * 0.3;
                emit(level, sparkle(k % 2 == 0 ? InvertedSky.CYAN : InvertedSky.PALE, 0.12F, 26).gravity(0.03F).flags(FxOptions.FLICKER | FxOptions.HOT | FxOptions.BOUNCE),
                    this.centre.add(0.0, 0.3, 0.0), new Vec3(Math.cos(a) * s, 0.25 + random.nextDouble() * 0.3, Math.sin(a) * s));
            }
        }
    }

    static FxOptions mote(float size, int life) {
        return TheabyssModParticleTypes.SP_AURORA_MOTE.get().of(0xFFFFFF, size, life, 0);
    }

    static FxOptions star(float size, int life) {
        return TheabyssModParticleTypes.SP_SKY_STAR.get().of(0xFFFFFF, size, life, 0);
    }

    private static final class Riser {
        Vec3 base;
        int rgb;
        float height;
        int life;
        int age;
    }
}
