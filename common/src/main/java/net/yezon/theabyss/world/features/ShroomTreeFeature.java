package net.yezon.theabyss.world.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.DoubleUnaryOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;

public record ShroomTreeFeature(Variant variant, BlockState stemState, BlockState capState, BlockState gillState, Optional<HangingPlants> hanging) implements Feature {
    public enum Variant implements StringRepresentable {
        PARASOL("parasol"), BELL("bell"), TWIN("twin"), GROUP("group"), FORKED("forked"), TRIDENT("trident"), CROWN_BRANCH("crown_branch"),
        GIANT_FORK("giant_fork");

        public static final Codec<Variant> CODEC = StringRepresentable.fromEnum(Variant::values);
        private final String id;

        Variant(String id) {
            this.id = id;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }
    }

    public static final MapCodec<ShroomTreeFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Variant.CODEC.fieldOf("variant").forGetter(ShroomTreeFeature::variant),
        BlockState.CODEC.fieldOf("stem").forGetter(ShroomTreeFeature::stemState),
        BlockState.CODEC.fieldOf("cap").forGetter(ShroomTreeFeature::capState),
        BlockState.CODEC.fieldOf("gill").forGetter(ShroomTreeFeature::gillState),
        HangingPlants.CODEC.optionalFieldOf("hanging").forGetter(ShroomTreeFeature::hanging)
    ).apply(i, ShroomTreeFeature::new));
    public static final DeferredRegister<MapCodec<? extends Feature>> REGISTRY = DeferredRegister.create(Registries.FEATURE_TYPE, TheabyssMod.MODID);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ShroomTreeFeature>> SHROOM_TREE = REGISTRY.register("shroom_tree", () -> CODEC);

    private static final int STEM = 1;
    private static final int CAP = 2;
    private static final int GILL = 3;
    private static final double TAU = Math.PI * 2.0;
    private static final int MAX_EXTEND = 6;

    @Override
    public MapCodec<ShroomTreeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        Shape shape = new Shape(random);
        switch (this.variant) {
            case PARASOL -> shape.parasol();
            case BELL -> shape.bell();
            case TWIN -> shape.twin();
            case GROUP -> shape.group();
            case FORKED -> shape.forked();
            case TRIDENT -> shape.trident();
            case CROWN_BRANCH -> shape.crownBranch();
            case GIANT_FORK -> shape.giantFork();
        }
        int minDx = Integer.MAX_VALUE;
        int maxDx = Integer.MIN_VALUE;
        int minDz = Integer.MAX_VALUE;
        int maxDz = Integer.MIN_VALUE;
        for (long key : shape.blocks.keySet()) {
            int dx = BlockPos.getX(key);
            int dz = BlockPos.getZ(key);
            minDx = Math.min(minDx, dx);
            maxDx = Math.max(maxDx, dx);
            minDz = Math.min(minDz, dz);
            maxDz = Math.max(maxDz, dz);
        }
        int chunkX = SectionPos.blockToSectionCoord(origin.getX()) << 4;
        int chunkZ = SectionPos.blockToSectionCoord(origin.getZ()) << 4;
        int loX = Math.max(chunkX, chunkX - 16 - minDx);
        int hiX = Math.min(chunkX + 15, chunkX + 31 - maxDx);
        int loZ = Math.max(chunkZ, chunkZ - 16 - minDz);
        int hiZ = Math.min(chunkZ + 15, chunkZ + 31 - maxDz);
        if (loX > hiX || loZ > hiZ) {
            return false;
        }
        int x = Math.clamp(origin.getX(), loX, hiX);
        int z = Math.clamp(origin.getZ(), loZ, hiZ);
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        BlockPos base = new BlockPos(x, y, z);
        BlockState ground = level.getBlockState(base.below());
        if (ground.isAir() || !ground.getFluidState().isEmpty() || !ground.isFaceSturdy(level, base.below(), Direction.UP)) {
            return false;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (long key : shape.blocks.keySet()) {
            pos.set(x + BlockPos.getX(key), y + BlockPos.getY(key), z + BlockPos.getZ(key));
            BlockState here = level.getBlockState(pos);
            if (here.is(BlockTags.LOGS) || here.is(BlockTags.LEAVES)) {
                return false;
            }
        }
        List<BlockPos> placed = new ArrayList<>();
        for (Map.Entry<Long, int[]> entry : shape.blocks.entrySet()) {
            long key = entry.getKey();
            int kind = entry.getValue()[0];
            int dy = BlockPos.getY(key);
            pos.set(x + BlockPos.getX(key), y + dy, z + BlockPos.getZ(key));
            BlockState state = this.state(kind, entry.getValue()[1]);
            if (canReplace(level.getBlockState(pos))) {
                level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                placed.add(pos.immutable());
            }
            if (kind == STEM && dy == 0) {
                for (int down = 1; down <= MAX_EXTEND; down++) {
                    pos.move(Direction.DOWN);
                    if (!canReplace(level.getBlockState(pos))) {
                        break;
                    }
                    level.setBlock(pos, this.state(STEM, 1), Block.UPDATE_CLIENTS);
                }
            }
        }
        this.hanging.ifPresent(h -> h.decorate(level, random, placed));
        return true;
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir() || state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    private BlockState state(int kind, int axis) {
        BlockState state = kind == STEM ? this.stemState : kind == CAP ? this.capState : this.gillState;
        if (kind != CAP && state.hasProperty(BlockStateProperties.AXIS)) {
            state = state.setValue(BlockStateProperties.AXIS, axis == 0 ? Direction.Axis.X : axis == 2 ? Direction.Axis.Z : Direction.Axis.Y);
        }
        return state;
    }

    private static final class Shape {
        final Map<Long, int[]> blocks = new HashMap<>();
        final RandomSource rng;

        Shape(RandomSource rng) {
            this.rng = rng;
        }

        double uniform(double a, double b) {
            return a + (b - a) * this.rng.nextDouble();
        }

        int randint(int a, int b) {
            return a + this.rng.nextInt(b - a + 1);
        }

        void put(double x, double y, double z, int kind, int axis, boolean force) {
            long key = BlockPos.asLong((int)Math.round(x), (int)Math.round(y), (int)Math.round(z));
            int[] old = this.blocks.get(key);
            if (force || old == null || old[0] == GILL && kind != GILL) {
                this.blocks.put(key, new int[]{kind, axis});
            }
        }

        static double lerp(double a, double b, double t) {
            return a + (b - a) * t;
        }

        static DoubleUnaryOperator taper(double r0, double r1, double bulge, double foot) {
            return t -> lerp(r0, r1, t) + bulge * Math.sin(Math.PI * t) + foot * Math.pow(Math.max(0.0, 1.0 - t / 0.15), 2.0);
        }

        static List<double[]> bezier(double[][] points, int n) {
            List<double[]> out = new ArrayList<>(n + 1);
            for (int i = 0; i <= n; i++) {
                double t = (double)i / n;
                double[][] pts = points;
                while (pts.length > 1) {
                    double[][] next = new double[pts.length - 1][3];
                    for (int j = 0; j < pts.length - 1; j++) {
                        for (int k = 0; k < 3; k++) {
                            next[j][k] = lerp(pts[j][k], pts[j + 1][k], t);
                        }
                    }
                    pts = next;
                }
                out.add(pts[0]);
            }
            return out;
        }

        List<double[]> stem(double[][] points, DoubleUnaryOperator radius) {
            double length = 0.0;
            for (int i = 0; i < points.length - 1; i++) {
                length += Math.sqrt(sq(points[i + 1][0] - points[i][0]) + sq(points[i + 1][1] - points[i][1]) + sq(points[i + 1][2] - points[i][2]));
            }
            List<double[]> path = bezier(points, Math.max(10, (int)(length * 4.0)));
            for (int i = 0; i < path.size(); i++) {
                double[] p = path.get(i);
                double[] a = path.get(Math.max(0, i - 1));
                double[] b = path.get(Math.min(path.size() - 1, i + 1));
                double tx = Math.abs(b[0] - a[0]);
                double ty = Math.abs(b[1] - a[1]);
                double tz = Math.abs(b[2] - a[2]);
                int axis = ty >= tx && ty >= tz ? 1 : tx >= tz ? 0 : 2;
                double r = radius.applyAsDouble((double)i / (path.size() - 1));
                int ri = (int)Math.ceil(r) + 1;
                long cx = Math.round(p[0]);
                long cz = Math.round(p[2]);
                for (int dx = -ri; dx <= ri; dx++) {
                    for (int dz = -ri; dz <= ri; dz++) {
                        if (sq(dx + (p[0] - cx)) + sq(dz + (p[2] - cz)) <= r * r + 0.25) {
                            this.put(cx + dx, p[1], cz + dz, STEM, axis, false);
                        }
                    }
                }
            }
            return path;
        }

        void roots(double r, int count, double spread, double height) {
            for (int k = 0; k < count; k++) {
                double a = (k + this.uniform(-0.3, 0.3)) / count * TAU;
                double length = spread * this.uniform(0.75, 1.25);
                int steps = (int)(length * 4.0) + 2;
                int axis = Math.abs(Math.cos(a)) >= Math.abs(Math.sin(a)) ? 0 : 2;
                for (int s = 0; s <= steps; s++) {
                    double t = (double)s / steps;
                    double d = r * 0.8 + t * length;
                    double y = height * Math.pow(1.0 - t, 2.2);
                    this.put(Math.cos(a) * d, Math.round(y), Math.sin(a) * d, STEM, axis, false);
                }
            }
        }

        void cap(int cx, int cy, int cz, double radius, double height, double droop, int skirt, double umbo, double profile, int ribs, double tiltX,
                 double tiltZ) {
            double noise = 0.08;
            double[] wobble = new double[12];
            for (int i = 0; i < 12; i++) {
                wobble[i] = 1.0 + this.uniform(-noise, noise);
            }
            int ri = (int)Math.ceil(radius * (1.0 + noise)) + 1;
            Map<Long, double[]> top = new HashMap<>();
            for (int dx = -ri; dx <= ri; dx++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    double ang = ((Math.atan2(dz, dx) / TAU * 12.0) % 12.0 + 12.0) % 12.0;
                    int i0 = (int)ang;
                    double f = ang - i0;
                    double rr = radius * lerp(wobble[i0 % 12], wobble[(i0 + 1) % 12], f);
                    double r = Math.sqrt(dx * dx + dz * dz);
                    if (r > rr) {
                        continue;
                    }
                    double t = r / rr;
                    double y = height * (1.0 - Math.pow(t, profile)) + umbo * Math.max(0.0, 1.0 - t / 0.3)
                        - droop * sq(Math.max(0.0, (t - 0.6) / 0.4)) + tiltX * dx + tiltZ * dz;
                    top.put(key(dx, dz), new double[]{cy + Math.round(y), t});
                }
            }
            Map<Long, double[]> bottoms = new HashMap<>();
            for (Map.Entry<Long, double[]> e : top.entrySet()) {
                int dx = (int)(e.getKey() >> 32);
                int dz = (int)(long)e.getKey();
                int y = (int)e.getValue()[0];
                int low = y;
                int neighbours = 0;
                for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                    double[] n = top.get(key(dx + d[0], dz + d[1]));
                    if (n != null) {
                        neighbours++;
                        low = Math.min(low, (int)n[0] + 1);
                    }
                }
                boolean edge = neighbours < 4;
                int bottom = Math.min(y, low);
                if (edge && skirt > 0) {
                    bottom = Math.min(bottom, y - skirt - (this.rng.nextDouble() < 0.3 ? 1 : 0));
                }
                for (int yy = bottom; yy <= y; yy++) {
                    this.put(cx + dx, yy, cz + dz, CAP, 1, true);
                }
                bottoms.put(e.getKey(), new double[]{bottom, e.getValue()[1], edge ? 1 : 0});
            }
            for (Map.Entry<Long, double[]> e : bottoms.entrySet()) {
                double[] v = e.getValue();
                if (v[2] == 0 && v[1] > 0.1 && v[1] < 0.9) {
                    this.put(cx + (int)(e.getKey() >> 32), v[0] - 1, cz + (int)(long)e.getKey(), GILL, 1, false);
                }
            }
            if (ribs > 0) {
                double off = this.uniform(0.0, TAU);
                for (int k = 0; k < ribs; k++) {
                    double a = off + (double)k / ribs * TAU;
                    int axis = Math.abs(Math.cos(a)) >= Math.abs(Math.sin(a)) ? 0 : 2;
                    for (int s = 1; s < (int)(radius * 0.9); s++) {
                        int kx = (int)Math.round(Math.cos(a) * s);
                        int kz = (int)Math.round(Math.sin(a) * s);
                        double[] b = bottoms.get(key(kx, kz));
                        if (b != null) {
                            this.put(cx + kx, b[0] - 1, cz + kz, STEM, axis, true);
                        }
                    }
                }
            }
        }

        void disc(int cx, int y, int cz, double radius) {
            int ri = (int)radius + 1;
            for (int dx = -ri; dx <= ri; dx++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    if (Math.sqrt(dx * dx + dz * dz) <= radius) {
                        this.put(cx + dx, y, cz + dz, CAP, 1, false);
                    }
                }
            }
        }

        double[] forkTree(int trunkH, double trunkR, double[][] branches, int baseRoots) {
            double lx = this.uniform(-1.0, 1.0);
            double lz = this.uniform(-1.0, 1.0);
            List<double[]> path = this.stem(new double[][]{{0, 0, 0}, {lx * 0.4, trunkH * 0.5, lz * 0.4}, {lx, trunkH, lz}},
                taper(trunkR, trunkR * 0.8, 0.0, trunkR * 0.7));
            this.roots(trunkR, baseRoots, trunkR * 1.8 + 1.5, Math.max(1, (int)trunkR));
            double[] fork = path.get(path.size() - 1);
            for (double[] br : branches) {
                double a = br[0];
                double ox = Math.cos(a) * br[1];
                double oz = Math.sin(a) * br[1];
                double[] end = {fork[0] + ox, fork[1] + br[2], fork[2] + oz};
                double[] mid = {fork[0] + ox * 0.75, fork[1] + br[2] * 0.3, fork[2] + oz * 0.75};
                this.stem(new double[][]{{fork[0], fork[1] - 1, fork[2]}, mid, end}, taper(br[3], br[3] * 0.75, 0.0, 0.0));
                this.cap((int)Math.round(end[0]), (int)Math.round(end[1]) + 1, (int)Math.round(end[2]), br[4], br[5], br[6], 1, 0.0, 1.6, (int)br[7],
                    Math.cos(a) * 0.06, Math.sin(a) * 0.06);
            }
            return fork;
        }

        void parasol() {
            int h = this.randint(12, 16);
            double lx = this.uniform(-2.0, 2.0);
            double lz = this.uniform(-2.0, 2.0);
            List<double[]> path = this.stem(new double[][]{{0, 0, 0}, {lx * 0.1, h * 0.45, lz * 0.1}, {lx, h, lz}}, taper(1.3, 0.9, 0.0, 1.2));
            this.roots(1.3, 5, 3.0, 1.0);
            double[] ring = path.get((int)(path.size() * 0.72));
            this.disc((int)Math.round(ring[0]), (int)Math.round(ring[1]), (int)Math.round(ring[2]), 2.0);
            this.cap((int)Math.round(lx), h + 1, (int)Math.round(lz), this.uniform(6.5, 8.0), 1.8, 1.8, 1, 1.2, 1.3, 8, 0.0, 0.0);
        }

        void bell() {
            int h = this.randint(10, 13);
            this.stem(new double[][]{{0, 0, 0}, {this.uniform(-1.0, 1.0), h, this.uniform(-1.0, 1.0)}}, taper(1.2, 0.8, 0.0, 1.0));
            this.roots(1.2, 4, 2.5, 1.0);
            this.cap(0, h - 2, 0, this.uniform(5.0, 6.5), this.uniform(5.5, 7.5), 0.0, 2, 0.0, 2.6, 0, 0.0, 0.0);
        }

        void twin() {
            double a = this.uniform(0.0, TAU);
            this.forkTree(this.randint(6, 8), 1.4, new double[][]{
                {a, this.uniform(4.6, 5.4), this.randint(6, 8), 1.0, this.uniform(3.8, 4.6), 2.0, 1.4, 0},
                {a + Math.PI + this.uniform(-0.35, 0.35), this.uniform(4.0, 4.8), this.randint(2, 4), 0.9, this.uniform(3.0, 3.8), 1.8, 1.2, 0}
            }, 5);
        }

        void group() {
            int n = this.randint(2, 3);
            this.stem(new double[][]{{0, 0, 0}, {0, 2, 0}}, t -> 1.8 - t * 0.4);
            this.roots(1.8, 5, 2.5, 1.0);
            double a0 = this.uniform(0.0, TAU);
            List<Integer> pool = new ArrayList<>();
            for (int v = 7; v < 15; v++) {
                pool.add(v);
            }
            List<Integer> heights = new ArrayList<>();
            for (int k = 0; k < n; k++) {
                heights.add(pool.remove(this.rng.nextInt(pool.size())));
            }
            heights.sort((p, q) -> q - p);
            for (int k = 0; k < n; k++) {
                double a = a0 + (double)k / n * TAU + this.uniform(-0.3, 0.3);
                int h = heights.get(k);
                double d = 2.0 + (15 - h) * 0.7;
                double[] end = {Math.cos(a) * d, h, Math.sin(a) * d};
                this.stem(new double[][]{{0, 1, 0}, {end[0] * 0.2, h * 0.6, end[2] * 0.2}, end}, taper(1.0, 0.7, 0.0, 0.0));
                this.cap((int)Math.round(end[0]), h + 1, (int)Math.round(end[2]), 2.8 + h * 0.15, this.uniform(1.8, 2.6), 1.0, 1, 0.0, 1.8, 0,
                    Math.cos(a) * 0.1, Math.sin(a) * 0.1);
            }
        }

        void forked() {
            double a = this.uniform(0.0, TAU);
            this.forkTree(this.randint(10, 13), 2.2, new double[][]{
                {a, this.uniform(8.0, 9.0), this.randint(9, 12), 1.5, this.uniform(6.5, 7.5), 2.4, 2.0, 8},
                {a + Math.PI + this.uniform(-0.35, 0.35), this.uniform(6.5, 7.5), this.randint(4, 7), 1.3, this.uniform(5.0, 6.0), 2.0, 1.8, 6}
            }, 7);
        }

        void trident() {
            double a = this.uniform(0.0, TAU);
            int[] rises = {this.randint(10, 13), this.randint(6, 9), this.randint(2, 4)};
            double[] sizes = {6.2, 5.0, 4.2};
            double[][] branches = new double[3][];
            for (int k = 0; k < 3; k++) {
                double ang = a + k * TAU / 3.0 + this.uniform(-0.3, 0.3);
                double r = sizes[k];
                branches[k] = new double[]{ang, r * 1.25 + this.uniform(1.0, 2.0), rises[k], 1.3 - k * 0.15, r * this.uniform(0.92, 1.08), 2.2, 1.8, 6};
            }
            this.forkTree(this.randint(10, 13), 2.4, branches, 8);
        }

        void crownBranch() {
            int h = this.randint(18, 22);
            double a = this.uniform(0.0, TAU);
            List<double[]> path = this.stem(new double[][]{{0, 0, 0}, {this.uniform(-1.5, 1.5), h * 0.5, this.uniform(-1.5, 1.5)}, {0, h, 0}},
                taper(2.2, 1.4, 0.0, 1.6));
            this.roots(2.2, 7, 4.5, 2.0);
            this.cap(0, h + 1, 0, this.uniform(6.5, 7.5), 2.6, 2.0, 1, 0.0, 1.5, 8, 0.0, 0.0);
            int sides = this.rng.nextInt(3) == 0 ? 1 : 2;
            for (int k = 0; k < sides; k++) {
                double[] start = path.get((int)(path.size() * this.uniform(0.38, 0.52)));
                double ang = a + k * Math.PI + this.uniform(-0.4, 0.4);
                double out = this.uniform(10.0, 11.5);
                int rise = this.randint(3, 6);
                double[] end = {start[0] + Math.cos(ang) * out, start[1] + rise, start[2] + Math.sin(ang) * out};
                double[] mid = {start[0] + Math.cos(ang) * out * 0.8, start[1] + 0.5, start[2] + Math.sin(ang) * out * 0.8};
                this.stem(new double[][]{start, mid, end}, taper(1.2, 0.85, 0.0, 0.0));
                this.cap((int)Math.round(end[0]), (int)Math.round(end[1]) + 1, (int)Math.round(end[2]), this.uniform(4.0, 5.0), 2.0, 1.4, 1, 0.0, 1.6, 0,
                    Math.cos(ang) * 0.08, Math.sin(ang) * 0.08);
            }
        }

        void giantFork() {
            double a = this.uniform(0.0, TAU);
            double[] fork = this.forkTree(this.randint(12, 15), 3.0, new double[][]{
                {a, this.uniform(9.5, 10.5), this.randint(12, 15), 1.8, this.uniform(8.0, 9.0), 2.8, 2.4, 10}
            }, 9);
            double a2 = a + Math.PI + this.uniform(-0.5, 0.5);
            double[] midPt = {fork[0] + Math.cos(a2) * 5.0, fork[1] + 3.0, fork[2] + Math.sin(a2) * 5.0};
            this.stem(new double[][]{{fork[0], fork[1] - 2.0, fork[2]}, {fork[0] + Math.cos(a2) * 3.5, fork[1], fork[2] + Math.sin(a2) * 3.5}, midPt},
                taper(1.7, 1.4, 0.0, 0.0));
            double[][] subs = {{1.0, this.randint(7, 9), this.uniform(5.5, 6.5)}, {-1.0, this.randint(2, 4), this.uniform(4.5, 5.5)}};
            for (double[] sub : subs) {
                double ang = a2 + sub[0];
                double r = sub[2];
                double[] end = {midPt[0] + Math.cos(ang) * (r + 3.0), midPt[1] + sub[1], midPt[2] + Math.sin(ang) * (r + 3.0)};
                double[] m = {midPt[0] + Math.cos(ang) * r * 0.8, midPt[1] + sub[1] * 0.3, midPt[2] + Math.sin(ang) * r * 0.8};
                this.stem(new double[][]{midPt, m, end}, taper(1.2, 0.9, 0.0, 0.0));
                this.cap((int)Math.round(end[0]), (int)Math.round(end[1]) + 1, (int)Math.round(end[2]), r, 2.2, 1.8, 1, 0.0, 1.6, 6,
                    Math.cos(ang) * 0.06, Math.sin(ang) * 0.06);
            }
        }

        static double sq(double v) {
            return v * v;
        }

        static long key(int dx, int dz) {
            return ((long)dx << 32) | (dz & 0xFFFFFFFFL);
        }
    }
}
