package net.yezon.theabyss.world.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;

public record LeafBushFeature(BlockState leaves, BlockState carpet, int carpetReach) implements Feature {
    public static final MapCodec<LeafBushFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        BlockState.CODEC.fieldOf("leaves").forGetter(LeafBushFeature::leaves),
        BlockState.CODEC.fieldOf("carpet").forGetter(LeafBushFeature::carpet),
        Codec.intRange(0, 6).fieldOf("carpet_reach").forGetter(LeafBushFeature::carpetReach)
    ).apply(i, LeafBushFeature::new));
    public static final DeferredRegister<MapCodec<? extends Feature>> REGISTRY = DeferredRegister.create(Registries.FEATURE_TYPE, TheabyssMod.MODID);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<LeafBushFeature>> LEAF_BUSH = REGISTRY.register("leaf_bush", () -> CODEC);

    private static final int MIN_LEAVES = 6;
    private static final int MAX_LEAVES = 12;
    private static final int[][] NEIGHBOURS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int MAX_STEP = 2;

    @Override
    public MapCodec<LeafBushFeature> codec() {
        return CODEC;
    }

    private static boolean isGround(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isFaceSturdy(level, pos, Direction.UP) && !state.is(BlockTags.LEAVES) && !state.is(BlockTags.LOGS);
    }

    private static boolean free(BlockState state) {
        return state.isAir() || state.canBeReplaced() && state.getFluidState().isEmpty() && !(state.getBlock() instanceof DoublePlantBlock);
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        int baseY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, origin.getX(), origin.getZ());
        BlockPos base = new BlockPos(origin.getX(), baseY, origin.getZ());
        if (!level.getFluidState(base).isEmpty() || !isGround(level, base.below())) {
            return false;
        }
        Shape shape = new Shape(random);
        shape.build();
        shape.leaves.sort(Comparator.comparingInt(l -> l[1]));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int[] leaf : shape.leaves) {
            int x = base.getX() + leaf[0];
            int z = base.getZ() + leaf[2];
            int ground = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
            if (Math.abs(ground - base.getY()) > MAX_STEP) {
                continue;
            }
            pos.set(x, ground + leaf[1], z);
            BlockPos below = pos.below();
            boolean supported = leaf[1] == 0 ? isGround(level, below) : level.getBlockState(below).is(this.leaves.getBlock());
            if (supported && free(level.getBlockState(pos)) && level.getFluidState(pos).isEmpty()) {
                level.setBlock(pos, this.leaves, Block.UPDATE_CLIENTS);
            }
        }
        for (int[] spot : shape.carpets(this.carpetReach)) {
            int x = base.getX() + spot[0];
            int z = base.getZ() + spot[1];
            int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
            if (Math.abs(y - base.getY()) > MAX_STEP) {
                continue;
            }
            pos.set(x, y, z);
            if (free(level.getBlockState(pos)) && this.carpet.canSurvive(level, pos)) {
                level.setBlock(pos, this.carpet, Block.UPDATE_CLIENTS);
            }
        }
        return true;
    }

    private static final class Shape {
        final RandomSource rng;
        final Set<Long> cells = new LinkedHashSet<>();
        final List<int[]> leaves = new ArrayList<>();
        final long noiseSeed;

        Shape(RandomSource rng) {
            this.rng = rng;
            this.noiseSeed = rng.nextLong();
        }

        static long key(int x, int y, int z) {
            return BlockPos.asLong(x, y, z);
        }

        boolean has(int x, int y, int z) {
            return this.cells.contains(key(x, y, z));
        }

        void add(int x, int y, int z) {
            if (y == 0 || this.has(x, y - 1, z)) {
                this.cells.add(key(x, y, z));
            }
        }

        int between(int a, int b) {
            return a + this.rng.nextInt(b - a + 1);
        }

        List<int[]> sample(int count) {
            List<int[]> list = new ArrayList<>(List.of(NEIGHBOURS));
            for (int i = list.size() - 1; i > 0; i--) {
                int j = this.rng.nextInt(i + 1);
                int[] t = list.get(i);
                list.set(i, list.get(j));
                list.set(j, t);
            }
            return list.subList(0, count);
        }

        List<int[]> footprint() {
            List<int[]> out = new ArrayList<>();
            for (long c : this.cells) {
                if (BlockPos.getY(c) == 0) {
                    out.add(new int[] {BlockPos.getX(c), BlockPos.getZ(c)});
                }
            }
            return out;
        }

        void build() {
            switch (this.rng.nextInt(6)) {
                case 0 -> this.mound();
                case 1 -> this.hedge();
                case 2 -> this.clump();
                case 3 -> this.sprawl();
                case 4 -> this.twin();
                default -> this.dome();
            }
            this.fit();
            int rot = this.rng.nextInt(4);
            boolean flip = this.rng.nextBoolean();
            for (long c : this.cells) {
                int x = BlockPos.getX(c);
                int z = BlockPos.getZ(c);
                if (flip) {
                    x = -x;
                }
                for (int i = 0; i < rot; i++) {
                    int t = x;
                    x = -z;
                    z = t;
                }
                this.leaves.add(new int[] {x, BlockPos.getY(c), z});
            }
        }

        void mound() {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (Math.abs(x) + Math.abs(z) < 2 || this.rng.nextFloat() < 0.55F) {
                        this.add(x, 0, z);
                    }
                }
            }
            this.add(0, 1, 0);
            for (int[] d : this.sample(this.between(0, 2))) {
                this.add(d[0], 1, d[1]);
            }
        }

        void hedge() {
            int length = this.between(4, 5);
            for (int i = 0; i < length; i++) {
                this.add(i, 0, 0);
                if (this.rng.nextFloat() < 0.45F) {
                    this.add(i, 0, this.rng.nextBoolean() ? 1 : -1);
                }
            }
            for (int i = 1; i < length - 1; i++) {
                if (this.rng.nextFloat() < 0.6F) {
                    this.add(i, 1, 0);
                }
            }
        }

        void clump() {
            for (int x = 0; x <= 1; x++) {
                for (int z = 0; z <= 1; z++) {
                    this.add(x, 0, z);
                    this.add(x, 1, z);
                }
            }
            int[][] sides = {{0, -1}, {1, -1}, {2, 0}, {2, 1}, {0, 2}, {1, 2}, {-1, 0}, {-1, 1}};
            int extra = this.between(1, 3);
            for (int i = 0; i < extra; i++) {
                int[] s = sides[this.rng.nextInt(sides.length)];
                this.add(s[0], 0, s[1]);
            }
            if (this.rng.nextFloat() < 0.7F) {
                this.add(this.rng.nextInt(2), 2, this.rng.nextInt(2));
            }
        }

        void sprawl() {
            List<int[]> cells = new ArrayList<>();
            Set<Long> seen = new HashSet<>();
            cells.add(new int[] {0, 0});
            seen.add(key(0, 0, 0));
            int target = this.between(8, 11);
            while (cells.size() < target) {
                int[] c = cells.get(this.rng.nextInt(cells.size()));
                int[] d = NEIGHBOURS[this.rng.nextInt(4)];
                if (seen.add(key(c[0] + d[0], 0, c[1] + d[1]))) {
                    cells.add(new int[] {c[0] + d[0], c[1] + d[1]});
                }
            }
            for (int[] c : cells) {
                this.add(c[0], 0, c[1]);
            }
            int tops = this.between(1, 2);
            for (int i = 0; i < tops; i++) {
                int[] c = cells.get(this.rng.nextInt(cells.size()));
                this.add(c[0], 1, c[1]);
            }
        }

        void twin() {
            int gap = this.between(3, 4);
            for (int cx : new int[] {0, gap}) {
                this.add(cx, 0, 0);
                for (int[] d : this.sample(this.between(2, 3))) {
                    this.add(cx + d[0], 0, d[1]);
                }
                this.add(cx, 1, 0);
            }
            for (int x = 1; x < gap; x++) {
                this.add(x, 0, 0);
            }
        }

        void dome() {
            this.add(0, 0, 0);
            for (int[] d : NEIGHBOURS) {
                this.add(d[0], 0, d[1]);
            }
            this.add(0, 1, 0);
            for (int[] d : this.sample(this.between(2, 3))) {
                this.add(d[0], 1, d[1]);
            }
            this.add(0, 2, 0);
        }

        void fit() {
            while (this.cells.size() > MAX_LEAVES) {
                int top = this.cells.stream().mapToInt(BlockPos::getY).max().orElse(0);
                List<Long> open = new ArrayList<>();
                for (long c : this.cells) {
                    if (!this.has(BlockPos.getX(c), BlockPos.getY(c) + 1, BlockPos.getZ(c))) {
                        open.add(c);
                    }
                }
                open.sort(Comparator.comparingInt((Long c) -> -BlockPos.getY(c))
                    .thenComparingInt(c -> -(Math.abs(BlockPos.getX(c)) + Math.abs(BlockPos.getZ(c)))));
                long first = open.get(0);
                this.cells.remove(BlockPos.getY(first) == top ? first : open.get(this.rng.nextInt(open.size())));
            }
            while (this.cells.size() < MIN_LEAVES) {
                List<int[]> foot = this.footprint();
                int[] c = foot.get(this.rng.nextInt(foot.size()));
                int[] d = NEIGHBOURS[this.rng.nextInt(4)];
                this.add(c[0] + d[0], 0, c[1] + d[1]);
            }
        }

        double hash(int ix, int iz) {
            long h = this.noiseSeed ^ (ix * 0x9E3779B97F4A7C15L) ^ (iz * 0xC2B2AE3D27D4EB4FL);
            h ^= h >>> 33;
            h *= 0xFF51AFD7ED558CCDL;
            h ^= h >>> 33;
            return (h >>> 11) * 0x1.0p-53;
        }

        double noise(double x, double z) {
            double fx = x / 2.3;
            double fz = z / 2.3;
            int ix = (int)Math.floor(fx);
            int iz = (int)Math.floor(fz);
            double tx = fx - ix;
            double tz = fz - iz;
            tx = tx * tx * (3 - 2 * tx);
            tz = tz * tz * (3 - 2 * tz);
            double a = this.hash(ix, iz) + (this.hash(ix + 1, iz) - this.hash(ix, iz)) * tx;
            double c = this.hash(ix, iz + 1) + (this.hash(ix + 1, iz + 1) - this.hash(ix, iz + 1)) * tx;
            return a + (c - a) * tz;
        }

        List<int[]> carpets(int reach) {
            Set<Long> foot = new HashSet<>();
            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxZ = Integer.MIN_VALUE;
            for (int[] leaf : this.leaves) {
                if (leaf[1] == 0) {
                    foot.add(key(leaf[0], 0, leaf[2]));
                    minX = Math.min(minX, leaf[0]);
                    maxX = Math.max(maxX, leaf[0]);
                    minZ = Math.min(minZ, leaf[2]);
                    maxZ = Math.max(maxZ, leaf[2]);
                }
            }
            List<int[]> out = new ArrayList<>();
            for (int x = minX - reach; x <= maxX + reach; x++) {
                for (int z = minZ - reach; z <= maxZ + reach; z++) {
                    if (foot.contains(key(x, 0, z))) {
                        continue;
                    }
                    double d = Double.MAX_VALUE;
                    for (long f : foot) {
                        d = Math.min(d, Math.hypot(x - BlockPos.getX(f), z - BlockPos.getZ(f)));
                    }
                    if (d > reach) {
                        continue;
                    }
                    double p = 0.9 * Math.exp(-(d - 1.0) / 1.25) * (0.35 + 1.3 * this.noise(x, z));
                    if (this.rng.nextDouble() < p) {
                        out.add(new int[] {x, z});
                    }
                }
            }
            return out;
        }
    }
}
