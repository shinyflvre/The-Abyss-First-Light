package net.yezon.theabyss.world.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.yezon.theabyss.init.TheabyssModBlocks;

public record MagmaFissures(String mode, int minSize, int maxSize, int rim, int columns, float crackChance, int branches, float dripstoneChance,
                             int dripstoneMin, int dripstoneMax) implements AbyssFeature.Config {
    public static final MapCodec<MagmaFissures> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("mode").forGetter(MagmaFissures::mode),
            Codec.INT.optionalFieldOf("min_size", 1).forGetter(MagmaFissures::minSize),
            Codec.INT.optionalFieldOf("max_size", 3).forGetter(MagmaFissures::maxSize),
            Codec.INT.optionalFieldOf("rim", 2).forGetter(MagmaFissures::rim),
            Codec.INT.optionalFieldOf("columns", 4).forGetter(MagmaFissures::columns),
            Codec.FLOAT.optionalFieldOf("crack_chance", 0.4F).forGetter(MagmaFissures::crackChance),
            Codec.INT.optionalFieldOf("branches", 2).forGetter(MagmaFissures::branches),
            Codec.FLOAT.optionalFieldOf("dripstone_chance", 0.0F).forGetter(MagmaFissures::dripstoneChance),
            Codec.INT.optionalFieldOf("dripstone_min", 1).forGetter(MagmaFissures::dripstoneMin),
            Codec.INT.optionalFieldOf("dripstone_max", 4).forGetter(MagmaFissures::dripstoneMax))
        .apply(i, MagmaFissures::new));
    private static final int REACH = 22;
    private static final int MAX_DEPTH = 1;
    private static final int STEP = 1;
    private static final float WIDE = 0.7F;
    private static final int[][] SIDES = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    @Override
    public MapCodec<MagmaFissures> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if ("crack".equals(this.mode)) {
            int length = size(random);
            float angle = random.nextFloat() * Mth.TWO_PI;
            int cx = centre(origin.getX());
            int cz = centre(origin.getZ());
            int sx = cx - Mth.floor(Mth.cos(angle) * length * 0.3F);
            int sz = cz - Mth.floor(Mth.sin(angle) * length * 0.3F);
            return crack(level, random, origin, sx, sz, length, angle, this.branches, Set.of());
        }
        return delta(level, random, origin);
    }

    private int size(RandomSource random) {
        return this.minSize + random.nextInt(Math.max(1, this.maxSize - this.minSize + 1));
    }

    private static int centre(int coord) {
        return (coord & ~15) + 8;
    }

    private static long key(int x, int z) {
        return ((long)x << 32) | (z & 0xFFFFFFFFL);
    }

    private static int kx(long key) {
        return (int)(key >> 32);
    }

    private static int kz(long key) {
        return (int)key;
    }

    private static int top(WorldGenLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
    }

    private static boolean natural(BlockState state) {
        return state.is(TheabyssModBlocks.STONE.get()) || state.is(TheabyssModBlocks.GRAVEL.get()) || state.is(TheabyssModBlocks.STONE_RACKLE.get())
            || state.is(TheabyssModBlocks.UNSTABLE_OBSIDIAN.get());
    }

    private static boolean solid(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && state.getFluidState().isEmpty() && !state.canBeReplaced();
    }

    private static boolean reach(BlockPos origin, int x, int z) {
        return Math.abs(x - centre(origin.getX())) <= REACH && Math.abs(z - centre(origin.getZ())) <= REACH;
    }

    private static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (!level.getBlockState(pos).is(Blocks.BEDROCK)) {
            level.setBlock(pos, state, 2);
        }
    }

    private static BlockState magma() {
        return TheabyssModBlocks.INFUSED_LAVA.get().defaultBlockState();
    }

    private static BlockState rackle() {
        return TheabyssModBlocks.STONE_RACKLE.get().defaultBlockState();
    }

    private static void crust(WorldGenLevel level, RandomSource random, BlockPos pos, float obsidian, float rackle, float gravel) {
        float roll = random.nextFloat();
        if (roll < obsidian) {
            set(level, pos, TheabyssModBlocks.UNSTABLE_OBSIDIAN.get().defaultBlockState());
        } else if (roll < obsidian + rackle) {
            set(level, pos, rackle());
        } else if (roll < obsidian + rackle + gravel) {
            set(level, pos, TheabyssModBlocks.GRAVEL.get().defaultBlockState());
        }
    }

    private boolean delta(WorldGenLevel level, RandomSource random, BlockPos origin) {
        int radius = size(random);
        int pool = top(level, origin.getX(), origin.getZ());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        if (!natural(level.getBlockState(pos.set(origin.getX(), pool, origin.getZ())))) {
            return false;
        }
        Set<Long> cells = new LinkedHashSet<>();
        for (int dx = -radius - 1; dx <= radius + 1; dx++) {
            for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                float d = Mth.sqrt(dx * dx + dz * dz) + (random.nextFloat() - 0.5F) * 1.6F;
                int t = top(level, x, z);
                if (d <= radius && (t == pool || t == pool + 1) && natural(level.getBlockState(pos.set(x, pool, z)))) {
                    cells.add(key(x, z));
                }
            }
        }
        sealed(level, cells, Set.of(), pool);
        if (cells.isEmpty()) {
            return false;
        }
        for (long c : cells) {
            int x = kx(c);
            int z = kz(c);
            for (int y = top(level, x, z); y > pool; y--) {
                set(level, pos.set(x, y, z), Blocks.AIR.defaultBlockState());
            }
            set(level, pos.set(x, pool, z), magma());
            if (!solid(level, pos.set(x, pool - 1, z))) {
                set(level, pos, TheabyssModBlocks.STONE.get().defaultBlockState());
            }
        }
        int rimSize = 1 + random.nextInt(Math.max(1, this.rim));
        List<Long> ring = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (long c : cells) {
            for (int dx = -rimSize; dx <= rimSize; dx++) {
                for (int dz = -rimSize; dz <= rimSize; dz++) {
                    long n = key(kx(c) + dx, kz(c) + dz);
                    if (cells.contains(n) || dx * dx + dz * dz > rimSize * rimSize + 1 || !seen.add(n)) {
                        continue;
                    }
                    int x = kx(n);
                    int z = kz(n);
                    if (natural(level.getBlockState(pos.set(x, top(level, x, z), z)))) {
                        crust(level, random, pos, 0.1F, 0.45F, 0.15F);
                        ring.add(n);
                    }
                }
            }
        }
        int pillars = this.columns <= 0 ? 0 : random.nextInt(this.columns + 1);
        for (int i = 0; i < pillars && !ring.isEmpty(); i++) {
            long n = ring.get(random.nextInt(ring.size()));
            int x = kx(n);
            int z = kz(n);
            int t = top(level, x, z);
            int height = 1 + random.nextInt(3) + (random.nextFloat() < 0.25F ? 1 : 0);
            for (int y = t + 1; y <= t + height; y++) {
                if (!level.getBlockState(pos.set(x, y, z)).isAir()) {
                    break;
                }
                set(level, pos, rackle());
            }
        }
        if (random.nextFloat() < this.crackChance) {
            List<Long> edge = new ArrayList<>(cells);
            int count = 1 + random.nextInt(2);
            for (int i = 0; i < count; i++) {
                long start = edge.get(random.nextInt(edge.size()));
                float angle = (float)Mth.atan2(kz(start) - origin.getZ(), kx(start) - origin.getX()) + (random.nextFloat() - 0.5F) * 0.8F;
                crack(level, random, origin, kx(start) + Mth.floor(Mth.cos(angle) * 2.5F), kz(start) + Mth.floor(Mth.sin(angle) * 2.5F),
                    16 + random.nextInt(14), angle, 1, cells);
            }
        }
        return true;
    }

    private static void sealed(WorldGenLevel level, Set<Long> cells, Set<Long> others, int fluidY) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean changed = true;
        for (int pass = 0; changed && pass < 8; pass++) {
            changed = false;
            for (long c : new ArrayList<>(cells)) {
                for (int[] s : SIDES) {
                    long n = key(kx(c) + s[0], kz(c) + s[1]);
                    if (cells.contains(n)) {
                        continue;
                    }
                    if (others.contains(n) || !solid(level, pos.set(kx(n), fluidY, kz(n)))) {
                        cells.remove(c);
                        changed = true;
                        break;
                    }
                }
            }
        }
    }

    private static void walk(RandomSource random, BlockPos origin, int x, int z, int length, float angle, int branches, List<Long> out) {
        float base = angle;
        float ex = 0.0F;
        float ez = 0.0F;
        int run = 0;
        int turn = 3 + random.nextInt(6);
        boolean wide = random.nextFloat() < WIDE;
        for (int i = 0; i < length; i++) {
            ex += Mth.cos(angle);
            ez += Mth.sin(angle);
            boolean stepX = Math.abs(ex) >= Math.abs(ez);
            if (stepX) {
                int s = ex > 0.0F ? 1 : -1;
                x += s;
                ex -= s;
            } else {
                int s = ez > 0.0F ? 1 : -1;
                z += s;
                ez -= s;
            }
            if (!reach(origin, x, z)) {
                return;
            }
            out.add(key(x, z));
            if (wide) {
                int wx = stepX ? x : x + 1;
                int wz = stepX ? z + 1 : z;
                if (reach(origin, wx, wz)) {
                    out.add(key(wx, wz));
                }
            }
            if (++run >= turn) {
                run = 0;
                turn = 3 + random.nextInt(7);
                angle = Mth.clamp(angle + (random.nextBoolean() ? 1 : -1) * Mth.PI / 4.0F, base - Mth.HALF_PI, base + Mth.HALF_PI);
                if (random.nextFloat() < 0.3F) {
                    wide = !wide;
                }
            }
            if (branches > 0 && i > 5 && i < length - 5 && random.nextFloat() < 0.08F) {
                branches--;
                float side = (random.nextBoolean() ? 1 : -1) * Mth.HALF_PI * (0.5F + random.nextFloat() * 0.5F);
                walk(random, origin, x, z, 8 + random.nextInt(12), angle + side, 0, out);
            }
        }
    }

    private boolean crack(WorldGenLevel level, RandomSource random, BlockPos origin, int startX, int startZ, int length, float angle, int branchCount,
                          Set<Long> avoid) {
        List<Long> path = new ArrayList<>();
        walk(random, origin, startX, startZ, length, angle, branchCount, path);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Set<Long> unique = new LinkedHashSet<>();
        for (long c : path) {
            if (avoid.contains(c)) {
                continue;
            }
            boolean touches = false;
            for (int[] s : SIDES) {
                touches |= avoid.contains(key(kx(c) + s[0], kz(c) + s[1]));
            }
            if (!touches) {
                unique.add(c);
            }
        }
        List<Set<Long>> segments = new ArrayList<>();
        Set<Long> current = new LinkedHashSet<>();
        int lo = Integer.MAX_VALUE;
        int hi = Integer.MIN_VALUE;
        for (long c : unique) {
            int t = top(level, kx(c), kz(c));
            if (!natural(level.getBlockState(pos.set(kx(c), t, kz(c))))) {
                if (!current.isEmpty()) {
                    segments.add(current);
                    current = new LinkedHashSet<>();
                }
                lo = Integer.MAX_VALUE;
                hi = Integer.MIN_VALUE;
                continue;
            }
            if (current.isEmpty() || Math.max(hi, t) - Math.min(lo, t) <= STEP) {
                current.add(c);
                lo = Math.min(lo, t);
                hi = Math.max(hi, t);
            } else {
                segments.add(current);
                current = new LinkedHashSet<>();
                lo = Integer.MAX_VALUE;
                hi = Integer.MIN_VALUE;
            }
        }
        if (!current.isEmpty()) {
            segments.add(current);
        }
        Set<Long> taken = new HashSet<>();
        for (Set<Long> segment : segments) {
            taken.addAll(segment);
        }
        boolean placed = false;
        for (Set<Long> segment : segments) {
            int low = Integer.MAX_VALUE;
            for (long c : segment) {
                low = Math.min(low, top(level, kx(c), kz(c)));
            }
            int fluidY = low;
            segment.removeIf(c -> top(level, kx(c), kz(c)) - fluidY > MAX_DEPTH);
            Set<Long> others = new HashSet<>(taken);
            others.removeAll(segment);
            sealed(level, segment, others, fluidY);
            if (segment.size() < 3) {
                taken.removeAll(segment);
                segment.clear();
                continue;
            }
            for (long c : segment) {
                int x = kx(c);
                int z = kz(c);
                for (int y = top(level, x, z); y > fluidY; y--) {
                    set(level, pos.set(x, y, z), Blocks.AIR.defaultBlockState());
                }
                set(level, pos.set(x, fluidY, z), magma());
                if (!solid(level, pos.set(x, fluidY - 1, z))) {
                    set(level, pos, TheabyssModBlocks.STONE.get().defaultBlockState());
                }
            }
            placed = true;
        }
        for (Set<Long> segment : segments) {
            for (long c : segment) {
                for (int[] s : SIDES) {
                    int x = kx(c) + s[0];
                    int z = kz(c) + s[1];
                    if (taken.contains(key(x, z))) {
                        continue;
                    }
                    int t = top(level, x, z);
                    if (natural(level.getBlockState(pos.set(x, t, z)))) {
                        crust(level, random, pos, 0.12F, 0.0F, 0.0F);
                        if (random.nextFloat() < this.dripstoneChance) {
                            DripstoneSpires.spire(level, x, t + 1, z,
                                this.dripstoneMin + random.nextInt(Math.max(1, this.dripstoneMax - this.dripstoneMin + 1)));
                        }
                    }
                }
            }
        }
        return placed;
    }
}
