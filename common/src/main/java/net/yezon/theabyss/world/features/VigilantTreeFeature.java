package net.yezon.theabyss.world.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public record VigilantTreeFeature(Variant variant, BlockState logState, BlockState leavesState, BlockState fruitState) implements AbyssFeature.Config {
    public enum Variant implements StringRepresentable {
        SPIRAL_SLIM("spiral_slim"), SPIRAL_TALL("spiral_tall"), STAIRCASE("staircase"), DOUBLE_HELIX("double_helix");

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

    public static final MapCodec<VigilantTreeFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Variant.CODEC.fieldOf("variant").forGetter(VigilantTreeFeature::variant),
        BlockState.CODEC.fieldOf("log").forGetter(VigilantTreeFeature::logState),
        BlockState.CODEC.fieldOf("leaves").forGetter(VigilantTreeFeature::leavesState),
        BlockState.CODEC.fieldOf("fruit").forGetter(VigilantTreeFeature::fruitState)
    ).apply(i, VigilantTreeFeature::new));
    public static final DeferredRegister<Feature<?>> REGISTRY = DeferredRegister.create(Registries.FEATURE, TheabyssMod.MODID);
    public static final DeferredHolder<Feature<?>, AbyssFeature<VigilantTreeFeature>> VIGILANT_TREE = REGISTRY.register(
        "vigilant_tree", () -> new AbyssFeature<>(CODEC)
    );

    private static final int LOG = 1;
    private static final int LEAF = 2;
    private static final int FRUIT = 3;
    private static final double TAU = Math.PI * 2.0;
    private static final int MAX_EXTEND = 6;

    @Override
    public MapCodec<VigilantTreeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        Shape shape = new Shape(random);
        switch (this.variant) {
            case SPIRAL_SLIM -> shape.spiralSlim();
            case SPIRAL_TALL -> shape.spiralTall();
            case STAIRCASE -> shape.staircase();
            case DOUBLE_HELIX -> shape.doubleHelix();
        }
        int minDx = Integer.MAX_VALUE;
        int maxDx = Integer.MIN_VALUE;
        int minDz = Integer.MAX_VALUE;
        int maxDz = Integer.MIN_VALUE;
        for (long key : shape.blocks.keySet()) {
            minDx = Math.min(minDx, BlockPos.getX(key));
            maxDx = Math.max(maxDx, BlockPos.getX(key));
            minDz = Math.min(minDz, BlockPos.getZ(key));
            maxDz = Math.max(maxDz, BlockPos.getZ(key));
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
        for (Map.Entry<Long, int[]> entry : shape.blocks.entrySet()) {
            long key = entry.getKey();
            int kind = entry.getValue()[0];
            int dy = BlockPos.getY(key);
            pos.set(x + BlockPos.getX(key), y + dy, z + BlockPos.getZ(key));
            if (canReplace(level.getBlockState(pos))) {
                level.setBlock(pos, this.state(kind, entry.getValue()[1]), Block.UPDATE_CLIENTS);
            }
            if (kind == LOG && dy == 0) {
                for (int down = 1; down <= MAX_EXTEND; down++) {
                    pos.move(Direction.DOWN);
                    if (!canReplace(level.getBlockState(pos))) {
                        break;
                    }
                    level.setBlock(pos, this.state(LOG, 1), Block.UPDATE_CLIENTS);
                }
            }
        }
        return true;
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir() || state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    private BlockState state(int kind, int axis) {
        if (kind == LEAF) {
            return this.leavesState;
        }
        if (kind == FRUIT) {
            return this.fruitState;
        }
        BlockState state = this.logState;
        if (state.hasProperty(BlockStateProperties.AXIS)) {
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

        void put(double x, double y, double z, int kind, int axis) {
            long key = BlockPos.asLong((int)Math.round(x), (int)Math.round(y), (int)Math.round(z));
            int[] old = this.blocks.get(key);
            if (old == null || old[0] == LEAF && kind == LOG) {
                this.blocks.put(key, new int[]{kind, axis});
            }
        }

        boolean has(int x, int y, int z) {
            return this.blocks.containsKey(BlockPos.asLong(x, y, z));
        }

        static double lerp(double a, double b, double t) {
            return a + (b - a) * t;
        }

        static double sq(double v) {
            return v * v;
        }

        static DoubleUnaryOperator taper(double r0, double r1, double foot) {
            return t -> lerp(r0, r1, t) + foot * sq(Math.max(0.0, 1.0 - t / 0.15));
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
                            this.put(cx + dx, p[1], cz + dz, LOG, axis);
                        }
                    }
                }
            }
            return path;
        }

        List<double[]> trunk(int h, double r0, double r1, double lean) {
            double lx = this.uniform(-lean, lean);
            double lz = this.uniform(-lean, lean);
            return this.stem(new double[][]{{0, 0, 0}, {lx * 0.3, h * 0.5, lz * 0.3}, {lx, h, lz}}, taper(r0, r1, r0 * 0.6));
        }

        static double[] at(List<double[]> path, double y) {
            double[] best = path.get(0);
            for (double[] p : path) {
                if (Math.abs(p[1] - y) < Math.abs(best[1] - y)) {
                    best = p;
                }
            }
            return best;
        }

        void pad(int cx, int y, int cz, double rx, double rz, int layers) {
            for (int ly = 0; ly < layers; ly++) {
                double shrink = ly == 0 ? 0.0 : 0.9;
                for (int dx = -(int)rx - 1; dx <= (int)rx + 1; dx++) {
                    for (int dz = -(int)rz - 1; dz <= (int)rz + 1; dz++) {
                        if (sq(dx / (rx - shrink + 0.01)) + sq(dz / (rz - shrink + 0.01)) <= 1.0 + this.uniform(-0.15, 0.15)) {
                            this.put(cx + dx, y + ly, cz + dz, LEAF, 1);
                        }
                    }
                }
            }
        }

        void fruits(double chance, int maxLen) {
            List<Long> leaves = new ArrayList<>();
            for (Map.Entry<Long, int[]> e : this.blocks.entrySet()) {
                if (e.getValue()[0] == LEAF) {
                    leaves.add(e.getKey());
                }
            }
            for (long key : leaves) {
                int x = BlockPos.getX(key);
                int y = BlockPos.getY(key);
                int z = BlockPos.getZ(key);
                if (this.has(x, y - 1, z) || this.rng.nextDouble() >= chance) {
                    continue;
                }
                int n = this.randint(1, maxLen);
                for (int i = 1; i <= n; i++) {
                    if (this.has(x, y - i, z)) {
                        break;
                    }
                    this.put(x, y - i, z, FRUIT, 1);
                }
            }
        }

        void ribbon(List<double[]> path, double y0, double y1, double a0, double turns, double rIn, double rOut) {
            int steps = (int)((y1 - y0) * 10.0);
            for (int s = 0; s <= steps; s++) {
                double f = (double)s / steps;
                double y = y0 + (y1 - y0) * f;
                double a = a0 + f * turns * TAU;
                double[] p = at(path, y);
                double taperR = rOut * (1.0 - 0.45 * f);
                for (int k = 0; k < 5; k++) {
                    double r = rIn + (taperR - rIn) * k / 4.0;
                    this.put(p[0] + Math.cos(a) * r, y, p[2] + Math.sin(a) * r, LEAF, 1);
                }
                if (s % 8 == 0) {
                    int axis = Math.abs(Math.cos(a)) >= Math.abs(Math.sin(a)) ? 0 : 2;
                    this.put(p[0] + Math.cos(a) * rIn * 0.5, y, p[2] + Math.sin(a) * rIn * 0.5, LOG, axis);
                }
            }
        }

        void spiralSlim() {
            int h = this.randint(11, 15);
            List<double[]> path = this.trunk(h, 0.6, 0.5, 0.8);
            double a = this.uniform(0.0, TAU);
            int y = this.randint(3, 4);
            while (y < h - 1) {
                double[] p = at(path, y);
                double reach = this.uniform(2.3, 2.9);
                double ex = p[0] + Math.cos(a) * reach;
                double ez = p[2] + Math.sin(a) * reach;
                this.stem(new double[][]{{p[0], y, p[2]}, {ex, y + 1, ez}}, t -> 0.4);
                this.pad((int)Math.round(ex), y + 1, (int)Math.round(ez), this.uniform(1.9, 2.4), this.uniform(1.6, 2.0), 2);
                a += this.uniform(1.6, 2.2);
                y += this.randint(2, 3);
            }
            double[] top = path.get(path.size() - 1);
            this.pad((int)Math.round(top[0]), h, (int)Math.round(top[2]), 1.8, 1.8, 3);
            this.fruits(0.3, 1);
        }

        void spiralTall() {
            int h = this.randint(17, 22);
            List<double[]> path = this.trunk(h, 1.1, 0.7, 1.2);
            double a = this.uniform(0.0, TAU);
            int y = this.randint(4, 5);
            while (y < h - 2) {
                double[] p = at(path, y);
                double frac = 1.0 - (double)y / h;
                double reach = 2.4 + frac * 2.5;
                double ex = p[0] + Math.cos(a) * reach;
                double ez = p[2] + Math.sin(a) * reach;
                this.stem(new double[][]{{p[0], y, p[2]}, {p[0] + Math.cos(a) * reach * 0.6, y + 1, p[2] + Math.sin(a) * reach * 0.6}, {ex, y + 2, ez}},
                    t -> 0.45);
                this.pad((int)Math.round(ex), y + 2, (int)Math.round(ez), 1.8 + frac * 1.2, 1.5 + frac * 1.0, 2);
                a += this.uniform(1.7, 2.1);
                y += this.randint(3, 4);
            }
            double[] top = path.get(path.size() - 1);
            this.pad((int)Math.round(top[0]), h, (int)Math.round(top[2]), 2.2, 2.2, 3);
            this.fruits(0.35, 2);
        }

        void staircase() {
            int h = this.randint(17, 21);
            List<double[]> path = this.trunk(h, 1.0, 0.6, 0.8);
            this.ribbon(path, 4, h - 1, this.uniform(0.0, TAU), this.uniform(2.2, 2.8), 1.2, this.uniform(4.2, 5.0));
            double[] top = path.get(path.size() - 1);
            this.pad((int)Math.round(top[0]), h, (int)Math.round(top[2]), 2.0, 2.0, 2);
            this.fruits(0.3, 2);
        }

        void doubleHelix() {
            int h = this.randint(15, 19);
            double a = this.uniform(0.0, TAU);
            double bx = Math.cos(a) * 2.5;
            double bz = Math.sin(a) * 2.5;
            List<double[]> path = this.stem(new double[][]{{0, 0, 0}, {bx, h * 0.5, bz}, {-bx * 0.3, h, -bz * 0.3}}, taper(0.8, 0.55, 0.5));
            double a0 = this.uniform(0.0, TAU);
            double turns = this.uniform(1.1, 1.3);
            this.ribbon(path, 3, h - 1, a0, turns, 1.0, 3.3);
            this.ribbon(path, 4, h - 1, a0 + Math.PI, turns, 1.0, 3.0);
            double[] top = path.get(path.size() - 1);
            this.pad((int)Math.round(top[0]), h, (int)Math.round(top[2]), 1.6, 1.6, 2);
            this.fruits(0.3, 1);
        }
    }
}
