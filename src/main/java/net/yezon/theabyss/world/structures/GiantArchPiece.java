package net.yezon.theabyss.world.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SpeleothemThickness;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class GiantArchPiece extends StructurePiece {
    private final GiantArchShape shape;
    private final GiantArchMaterials materials;

    public GiantArchPiece(GiantArchShape shape, GiantArchMaterials materials, BoundingBox box) {
        super(GiantArchStructure.GIANT_ARCH_PIECE.get(), 0, box);
        this.shape = shape;
        this.materials = materials;
    }

    public GiantArchPiece(CompoundTag tag) {
        super(GiantArchStructure.GIANT_ARCH_PIECE.get(), tag);
        this.shape = GiantArchShape.load(tag);
        this.materials = tag.read("Materials", GiantArchMaterials.CODEC).orElseThrow();
    }

    public GiantArchShape shape() {
        return this.shape;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        this.shape.save(tag);
        tag.store("Materials", GiantArchMaterials.CODEC, this.materials);
    }

    private static long hash(long seed, int x, int y, int z) {
        long h = seed ^ x * 0x9E3779B97F4A7C15L ^ y * 0xC2B2AE3D27D4EB4FL ^ z * 0x165667B19E3779F9L;
        h = (h ^ h >>> 30) * 0xBF58476D1CE4E5B9L;
        h = (h ^ h >>> 27) * 0x94D049BB133111EBL;
        return h ^ h >>> 31;
    }

    private static float chance(long seed, int x, int y, int z) {
        return (hash(seed, x, y, z) >>> 40) / (float)(1L << 24);
    }

    private static boolean solid(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && state.getFluidState().isEmpty() && !state.canBeReplaced();
    }

    private static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (!level.getBlockState(pos).is(Blocks.BEDROCK)) {
            level.setBlock(pos, state, 2);
        }
    }

    private BlockState rock(int x, int y, int z) {
        if (this.materials.vein().isPresent() && this.shape.vein(x, y, z)) {
            return this.materials.vein().get().defaultBlockState();
        }
        double value = this.shape.band(x, y, z);
        int band = Mth.floor(value);
        float pick = chance(this.shape.seed, 0, band, 7);
        if (pick >= 0.82F && value - band < 0.22) {
            return this.materials.seam().defaultBlockState();
        }
        if (pick < 0.62F) {
            return this.materials.rock().defaultBlockState();
        }
        return this.materials.band().defaultBlockState();
    }

    private void drip(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int y, int z, int length, int worldMin) {
        int free = 0;
        while (free < length && y - free - 1 >= worldMin && level.getBlockState(pos.set(x, y - free - 1, z)).isAir()) {
            free++;
        }
        BlockState drip = this.materials.drip().defaultBlockState();
        boolean speleothem = drip.hasProperty(BlockStateProperties.VERTICAL_DIRECTION) && drip.hasProperty(BlockStateProperties.SPELEOTHEM_THICKNESS);
        if (speleothem) {
            free = Math.min(free, 6);
        }
        for (int d = 1; d <= free; d++) {
            pos.set(x, y - d, z);
            BlockState state;
            if (speleothem) {
                SpeleothemThickness thickness = d == free ? SpeleothemThickness.TIP
                    : d == free - 1 ? SpeleothemThickness.FRUSTUM : d == 1 ? SpeleothemThickness.BASE : SpeleothemThickness.MIDDLE;
                state = drip.setValue(BlockStateProperties.VERTICAL_DIRECTION, Direction.DOWN).setValue(BlockStateProperties.SPELEOTHEM_THICKNESS, thickness);
            } else if (d == free && this.materials.dripTip().isPresent()) {
                state = this.materials.dripTip().get().defaultBlockState();
            } else {
                state = drip;
            }
            set(level, pos, state);
        }
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox chunkBB,
        ChunkPos chunkPos, BlockPos referencePos) {
        GiantArchShape shape = this.shape;
        shape.build();
        int samples = shape.count();
        int[] candidates = new int[samples];
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = Math.max(chunkBB.minX(), this.boundingBox.minX());
        int maxX = Math.min(chunkBB.maxX(), this.boundingBox.maxX());
        int minZ = Math.max(chunkBB.minZ(), this.boundingBox.minZ());
        int maxZ = Math.min(chunkBB.maxZ(), this.boundingBox.maxZ());
        int worldMin = Math.max(level.getMinY() + 1, chunkBB.minY());
        int worldMax = Math.min(level.getMaxY() - 1, chunkBB.maxY());
        double topY = shape.topY();
        double footReachA = shape.rLeg * shape.legA * 1.4 * Math.max(1.0, shape.widthRatio);
        double footReachB = shape.rLeg * shape.legB * 1.4 * Math.max(1.0, shape.widthRatio);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double cx = x + 0.5;
                double cz = z + 0.5;
                int found = 0;
                double low = Double.MAX_VALUE;
                double high = -Double.MAX_VALUE;
                for (int i = 0; i < samples; i++) {
                    double reach = shape.reach(i);
                    double ex = shape.sampleX(i) - cx;
                    double ez = shape.sampleZ(i) - cz;
                    if (ex * ex + ez * ez <= reach * reach) {
                        candidates[found++] = i;
                        low = Math.min(low, shape.sampleY(i) - reach);
                        high = Math.max(high, shape.sampleY(i) + reach);
                    }
                }
                if (found == 0) {
                    continue;
                }
                int lo = Math.max(worldMin, Mth.floor(low));
                int hi = Math.min(worldMax, Mth.ceil(high));
                if (lo > hi) {
                    continue;
                }
                boolean[] inside = new boolean[hi - lo + 3];
                for (int y = lo - 1; y <= hi + 1; y++) {
                    inside[y - lo + 1] = shape.distance(cx, y + 0.5, cz, candidates, found) < 0.0;
                }
                int lowest = Integer.MAX_VALUE;
                for (int y = hi; y >= lo; y--) {
                    if (!inside[y - lo + 1]) {
                        continue;
                    }
                    lowest = y;
                    boolean above = inside[y - lo + 2];
                    pos.set(x, y, z);
                    if (!above && y + 1 <= worldMax) {
                        set(level, pos, this.materials.top().defaultBlockState());
                        if (this.materials.crown().isPresent() && y > topY - 14 && chance(shape.seed, x, y, z) < 0.025F
                            && level.getBlockState(pos.above()).isAir()) {
                            set(level, pos.above(), this.materials.crown().get().defaultBlockState());
                        } else if (this.materials.pillar().isPresent() && chance(shape.seed, x, y, z + 29) < 0.006F) {
                            int height = 1 + (int)(chance(shape.seed, x, y, z + 31) * 4.0F);
                            for (int d = 1; d <= height + 1 && y + d <= worldMax; d++) {
                                pos.set(x, y + d, z);
                                if (!level.getBlockState(pos).isAir()) {
                                    break;
                                }
                                Block block = d <= height || this.materials.pillarCap().isEmpty() ? this.materials.pillar().get() : this.materials.pillarCap().get();
                                set(level, pos, block.defaultBlockState());
                            }
                        }
                    } else {
                        set(level, pos, rock(x, y, z));
                    }
                    if (!inside[y - lo] && y > lo && chance(shape.seed, x, y, z + 7) < 0.1F) {
                        drip(level, pos, x, y, z, 2 + (int)(chance(shape.seed, x, y, z + 13) * 7.0F), worldMin);
                    }
                }
                if (lowest == Integer.MAX_VALUE) {
                    continue;
                }
                double da = Math.sqrt((cx - shape.ax) * (cx - shape.ax) + (cz - shape.az) * (cz - shape.az));
                double db = Math.sqrt((cx - shape.bx) * (cx - shape.bx) + (cz - shape.bz) * (cz - shape.bz));
                boolean footA = da < footReachA && lowest <= shape.ay - GiantArchShape.LEG_DEPTH + 12;
                boolean footB = db < footReachB && lowest <= shape.by - GiantArchShape.LEG_DEPTH + 12;
                if (footA || footB) {
                    for (int y = lowest - 1; y > lowest - 80 && y >= worldMin; y--) {
                        pos.set(x, y, z);
                        if (solid(level, pos)) {
                            break;
                        }
                        set(level, pos, this.materials.rock().defaultBlockState());
                    }
                }
            }
        }
    }
}
