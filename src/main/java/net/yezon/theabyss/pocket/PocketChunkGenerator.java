package net.yezon.theabyss.pocket;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;
import org.jspecify.annotations.Nullable;

public class PocketChunkGenerator extends ChunkGenerator {
    public static final MapCodec<PocketChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)).apply(i, i.stable(PocketChunkGenerator::new))
    );
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> REGISTRY = DeferredRegister.create(Registries.CHUNK_GENERATOR, TheabyssMod.MODID);
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<PocketChunkGenerator>> POCKET_CELLS = REGISTRY.register(
        "pocket_cells", () -> CODEC
    );

    private static final int FLOOR = PocketCells.FLOOR;
    private static final int RING = PocketCells.RING;
    private static final int BOX = PocketCells.BOX;
    private static final int CELL = PocketCells.CELL;
    private static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();
    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState LILY_PAD = Blocks.LILY_PAD.defaultBlockState();
    private static final BlockState SHORT_GRASS = Blocks.SHORT_GRASS.defaultBlockState();
    private static final BlockState TALL_GRASS_LOWER = Blocks.TALL_GRASS.defaultBlockState()
        .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
    private static final BlockState TALL_GRASS_UPPER = Blocks.TALL_GRASS.defaultBlockState()
        .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
    private static final BlockState[] FLOWERS = {
        Blocks.DANDELION.defaultBlockState(), Blocks.POPPY.defaultBlockState(), Blocks.OXEYE_DAISY.defaultBlockState(),
        Blocks.AZURE_BLUET.defaultBlockState(), Blocks.CORNFLOWER.defaultBlockState()
    };
    private static final BlockState OAK_LOG = Blocks.OAK_LOG.defaultBlockState();
    private static final BlockState BIRCH_LOG = Blocks.BIRCH_LOG.defaultBlockState();
    private static final BlockState OAK_LEAVES = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    private static final BlockState BIRCH_LEAVES = Blocks.BIRCH_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);

    public PocketChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSets, RandomState randomState, long levelSeed) {
        return ChunkGeneratorStructureState.createForFlat(randomState, levelSeed, this.getOrigin(randomState), this.biomeSource, Stream.empty());
    }

    @Override
    public CompletableFuture<ChunkAccess> buildTerrain(
        ChunkAccess chunk,
        Blender blender,
        RandomState randomState,
        StructureManager structureManager,
        BiomeManager biomeManager,
        @Nullable WorldGenRegion carverBiomeRegion,
        Set<Holder<Biome>> possibleBiomes
    ) {
        ChunkPos pos = chunk.getPos();
        int minX = pos.x() * 16;
        int minZ = pos.z() * 16;
        int cell = PocketCells.at(minX, minZ);
        if (cell >= 0) {
            new Writer(chunk, new Land(cell), minX, minZ).write();
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
        int cell = PocketCells.at(x, z);
        if (cell < 0) {
            return heightAccessor.getMinY();
        }
        Land land = new Land(cell);
        int u = x - PocketCells.originX(cell);
        int v = z - PocketCells.originZ(cell);
        int h = land.height(u, v);
        return (h < FLOOR && type.isOpaque().test(WATER) ? FLOOR : h) + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        int minY = heightAccessor.getMinY();
        int cell = PocketCells.at(x, z);
        if (cell < 0) {
            return new NoiseColumn(minY, new BlockState[0]);
        }
        Land land = new Land(cell);
        int u = x - PocketCells.originX(cell);
        int v = z - PocketCells.originZ(cell);
        int h = land.height(u, v);
        int top = Math.max(h, FLOOR);
        BlockState[] column = new BlockState[top - minY + 1];
        Arrays.fill(column, Blocks.AIR.defaultBlockState());
        for (int y = PocketCells.BOTTOM; y <= top; y++) {
            column[y - minY] = y > h ? WATER : y == h ? land.surface(u, v, h) : y > h - 4 ? DIRT : y == PocketCells.BOTTOM ? BEDROCK : STONE;
        }
        return new NoiseColumn(minY, column);
    }

    @Override
    public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos, SamplerContext samplerContext) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor heightAccessor) {
        return FLOOR + 1;
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public int getSeaLevel() {
        return FLOOR;
    }

    static final class Land {
        private final long seed;
        private final double[][] ponds;
        private final int[][] trees;

        Land(int cell) {
            this.seed = mix(0x5EED_B0C5L + cell * 0x9E3779B97F4A7C15L);
            int side = (int)(unit(this.seed, 1, 0) * 4.0);
            boolean second = unit(this.seed, 2, 0) < 0.5;
            this.ponds = second ? new double[][] {this.pond(side, 3), this.pond((side + 2) % 4, 7)} : new double[][] {this.pond(side, 3)};
            this.trees = this.plantTrees();
        }

        private double[] pond(int side, int salt) {
            double along = 4.0 + unit(this.seed, salt, 1) * 2.5;
            double across = 2.5 + unit(this.seed, salt, 2) * 1.5;
            double offset = (unit(this.seed, salt, 3) - 0.5) * 18.0;
            double middle = RING + BOX / 2.0;
            double out = 3.0 + across;
            return switch (side) {
                case 0 -> new double[] {middle + offset, RING - out, along, across};
                case 1 -> new double[] {middle + offset, RING + BOX + out, along, across};
                case 2 -> new double[] {RING - out, middle + offset, across, along};
                default -> new double[] {RING + BOX + out, middle + offset, across, along};
            };
        }

        private int[][] plantTrees() {
            double chance = 0.35 + unit(this.seed, 3, 0) * 0.15;
            int[][] found = new int[64][];
            int count = 0;
            for (int gi = 0; gi < 8; gi++) {
                for (int gj = 0; gj < 8; gj++) {
                    long h = mix(this.seed ^ (gi * 31L + gj) * 0x632BE59BD9B4E019L);
                    if (((h >>> 40) & 1023) / 1024.0 >= chance) {
                        continue;
                    }
                    int u = gi * 8 + 1 + (int)((h >>> 8) % 6);
                    int v = gj * 8 + 1 + (int)((h >>> 16) % 6);
                    double d = boxDistance(u, v);
                    if (d < 4.0 || d > 14.0 || this.pondShore(u, v) > 0.0) {
                        continue;
                    }
                    boolean birch = ((h >>> 24) & 3) == 0;
                    int trunk = (birch ? 5 : 4) + (int)((h >>> 28) % 3);
                    found[count++] = new int[] {u, v, trunk, birch ? 1 : 0};
                }
            }
            return Arrays.copyOf(found, count);
        }

        int height(int u, int v) {
            double d = boxDistance(u, v);
            if (d <= 0.0) {
                return FLOOR;
            }
            int depth = this.pondDepth(u, v);
            if (depth > 0) {
                return FLOOR - depth;
            }
            double shore = this.pondShore(u, v);
            double hill = smooth(3.0, 15.0, d) * (4.0 + 6.0 * noise(this.seed, u / 13.0, v / 13.0)) * (1.0 - shore);
            double ripple = smooth(1.0, 5.0, d) * noise(this.seed + 1, u / 5.0, v / 5.0) * 2.2 * (1.0 - shore);
            return FLOOR + (int)Math.round(hill + ripple);
        }

        BlockState surface(int u, int v, int h) {
            return h < FLOOR || this.pondShore(u, v) > 0.55 ? SAND : GRASS;
        }

        int pondDepth(int u, int v) {
            double e = this.pondEllipse(u, v);
            return e < 1.0 ? 1 + (int)(2.2 * (1.0 - e)) : 0;
        }

        double pondShore(int u, int v) {
            double e = Math.sqrt(this.pondEllipse(u, v));
            return e < 1.0 ? 1.0 : Math.max(0.0, 1.0 - (e - 1.0) / 1.2);
        }

        private double pondEllipse(int u, int v) {
            double best = Double.MAX_VALUE;
            for (double[] p : this.ponds) {
                double a = (u + 0.5 - p[0]) / p[2];
                double b = (v + 0.5 - p[1]) / p[3];
                best = Math.min(best, a * a + b * b);
            }
            return best;
        }
    }

    private static final class Writer {
        private final ChunkAccess chunk;
        private final Land land;
        private final int minX;
        private final int minZ;
        private final int originX;
        private final int originZ;
        private final Heightmap oceanFloor;
        private final Heightmap worldSurface;
        private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        Writer(ChunkAccess chunk, Land land, int minX, int minZ) {
            this.chunk = chunk;
            this.land = land;
            this.minX = minX;
            this.minZ = minZ;
            int cell = PocketCells.at(minX, minZ);
            this.originX = PocketCells.originX(cell);
            this.originZ = PocketCells.originZ(cell);
            this.oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
            this.worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        }

        void write() {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    this.column(x, z);
                }
            }
            for (int[] tree : this.land.trees) {
                this.tree(tree);
            }
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    this.plants(x, z);
                }
            }
        }

        private void column(int x, int z) {
            int u = this.minX + x - this.originX;
            int v = this.minZ + z - this.originZ;
            int h = this.land.height(u, v);
            this.set(x, PocketCells.BOTTOM, z, BEDROCK);
            for (int y = PocketCells.BOTTOM + 1; y < h; y++) {
                this.set(x, y, z, y > h - 4 ? DIRT : STONE);
            }
            this.set(x, h, z, this.land.surface(u, v, h));
            for (int y = h + 1; y <= FLOOR; y++) {
                this.set(x, y, z, WATER);
            }
            if (h < FLOOR - 1 && unit(this.land.seed, u * 131 + v, 9) < 0.07) {
                this.set(x, FLOOR + 1, z, LILY_PAD);
            }
        }

        private void plants(int x, int z) {
            int u = this.minX + x - this.originX;
            int v = this.minZ + z - this.originZ;
            int h = this.land.height(u, v);
            if (!this.chunk.getBlockState(this.pos.set(x, h, z)).is(Blocks.GRASS_BLOCK) || !this.air(x, h + 1, z)) {
                return;
            }
            boolean inBox = boxDistance(u, v) <= 0.0;
            double roll = unit(this.land.seed, u * 257 + v, 5);
            if (inBox) {
                if (roll < 0.06) {
                    this.set(x, h + 1, z, SHORT_GRASS);
                }
                return;
            }
            double patch = noise(this.land.seed + 7, u / 4.0, v / 4.0);
            if (patch > 0.68 && roll < 0.45) {
                this.set(x, h + 1, z, FLOWERS[(int)(noise(this.land.seed + 11, u / 6.0, v / 6.0) * FLOWERS.length * 0.999)]);
            } else if (roll < 0.05 && this.air(x, h + 2, z)) {
                this.set(x, h + 1, z, TALL_GRASS_LOWER);
                this.set(x, h + 2, z, TALL_GRASS_UPPER);
            } else if (roll < 0.38) {
                this.set(x, h + 1, z, SHORT_GRASS);
            }
        }

        private void tree(int[] tree) {
            int u = tree[0];
            int v = tree[1];
            int trunk = tree[2];
            boolean birch = tree[3] == 1;
            int wx = this.originX + u;
            int wz = this.originZ + v;
            if (wx + 2 < this.minX || wx - 2 > this.minX + 15 || wz + 2 < this.minZ || wz - 2 > this.minZ + 15) {
                return;
            }
            int base = this.land.height(u, v) + 1;
            int top = base + trunk - 1;
            BlockState leaves = birch ? BIRCH_LEAVES : OAK_LEAVES;
            for (int y = top - 2; y <= top + 1; y++) {
                int radius = y >= top ? 1 : 2;
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        boolean corner = Math.abs(dx) == radius && Math.abs(dz) == radius;
                        if (corner && (y == top + 1 || unit(this.land.seed, (wx + dx) * 31 + wz + dz, y) < 0.5)) {
                            continue;
                        }
                        this.setIfAir(wx + dx - this.minX, y, wz + dz - this.minZ, leaves);
                    }
                }
            }
            for (int y = base; y <= top; y++) {
                this.setInside(wx - this.minX, y, wz - this.minZ, birch ? BIRCH_LOG : OAK_LOG);
            }
            this.setInside(wx - this.minX, base - 1, wz - this.minZ, DIRT);
        }

        private boolean air(int x, int y, int z) {
            return this.chunk.getBlockState(this.pos.set(x, y, z)).isAir();
        }

        private void setIfAir(int x, int y, int z, BlockState state) {
            if (x >= 0 && x < 16 && z >= 0 && z < 16 && this.air(x, y, z)) {
                this.set(x, y, z, state);
            }
        }

        private void setInside(int x, int y, int z, BlockState state) {
            if (x >= 0 && x < 16 && z >= 0 && z < 16) {
                this.set(x, y, z, state);
            }
        }

        private void set(int x, int y, int z, BlockState state) {
            this.chunk.setBlockState(this.pos.set(x, y, z), state);
            this.oceanFloor.update(x, y, z, state);
            this.worldSurface.update(x, y, z, state);
        }
    }

    static double boxDistance(int u, int v) {
        int dx = Math.max(Math.max(RING - u, u - (RING + BOX - 1)), 0);
        int dz = Math.max(Math.max(RING - v, v - (RING + BOX - 1)), 0);
        return Math.sqrt(dx * dx + dz * dz);
    }

    private static double smooth(double from, double to, double x) {
        double t = Math.clamp((x - from) / (to - from), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }

    private static double noise(long seed, double x, double z) {
        int x0 = (int)Math.floor(x);
        int z0 = (int)Math.floor(z);
        double fx = x - x0;
        double fz = z - z0;
        fx = fx * fx * (3.0 - 2.0 * fx);
        fz = fz * fz * (3.0 - 2.0 * fz);
        double a = unit(seed, x0, z0);
        double b = unit(seed, x0 + 1, z0);
        double c = unit(seed, x0, z0 + 1);
        double d = unit(seed, x0 + 1, z0 + 1);
        return (a + (b - a) * fx) + ((c + (d - c) * fx) - (a + (b - a) * fx)) * fz;
    }

    private static double unit(long seed, int a, int b) {
        return (mix(seed ^ a * 0x9E3779B97F4A7C15L ^ b * 0xC2B2AE3D27D4EB4FL) >>> 11) * 0x1.0p-53;
    }

    private static long mix(long z) {
        z = (z ^ z >>> 30) * 0xBF58476D1CE4E5B9L;
        z = (z ^ z >>> 27) * 0x94D049BB133111EBL;
        return z ^ z >>> 31;
    }
}
