package net.yezon.theabyss.world.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

public record FrostFormation(String shape, int minSize, int maxSize) implements Feature {
    public static final MapCodec<FrostFormation> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("shape").forGetter(FrostFormation::shape),
            Codec.INT.optionalFieldOf("min_size", 8).forGetter(FrostFormation::minSize),
            Codec.INT.optionalFieldOf("max_size", 16).forGetter(FrostFormation::maxSize))
        .apply(i, FrostFormation::new));
    private static final int REACH = 22;

    @Override
    public MapCodec<FrostFormation> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        return switch (this.shape) {
            case "spike" -> spike(level, random, center(origin).offset(random.nextInt(9) - 4, 0, random.nextInt(9) - 4), size(random));
            case "spike_field" -> spikeField(level, random, center(origin));
            case "foundation" -> foundation(level, random, center(origin));
            case "arch" -> arch(level, random, center(origin));
            default -> false;
        };
    }

    private int size(RandomSource random) {
        return this.minSize + random.nextInt(Math.max(1, this.maxSize - this.minSize + 1));
    }

    private static BlockPos center(BlockPos origin) {
        return new BlockPos((origin.getX() & ~15) + 8, origin.getY(), (origin.getZ() & ~15) + 8);
    }

    private static int ground(WorldGenLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
    }

    private static boolean solid(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && state.getFluidState().isEmpty() && !state.canBeReplaced();
    }

    private static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (level.getBlockState(pos).is(Blocks.BEDROCK)) {
            return;
        }
        level.setBlock(pos, state, 2);
    }

    private static BlockState stone() {
        return TheabyssModBlocks.DARK_STONE.get().defaultBlockState();
    }

    private static BlockState snow() {
        return TheabyssModBlocks.SNOW_BLOCK.get().defaultBlockState();
    }

    private static void anchor(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int startY, int z, BlockState state, int maxDepth) {
        for (int y = startY - 1; y > startY - maxDepth && y > level.getMinY(); y--) {
            pos.set(x, y, z);
            if (solid(level, pos)) {
                return;
            }
            set(level, pos, state);
        }
    }

    private boolean spikeField(WorldGenLevel level, RandomSource random, BlockPos origin) {
        int count = 3 + random.nextInt(5);
        boolean placed = false;
        for (int i = 0; i < count; i++) {
            BlockPos at = origin.offset(random.nextInt(17) - 8, 0, random.nextInt(17) - 8);
            int height = i == 0 ? this.maxSize - random.nextInt(4) : this.minSize + random.nextInt(Math.max(1, (this.maxSize - this.minSize) / 2 + 1));
            placed |= spike(level, random, at, height);
        }
        return placed;
    }

    private static boolean spike(WorldGenLevel level, RandomSource random, BlockPos origin, int height) {
        int baseY = ground(level, origin.getX(), origin.getZ());
        if (baseY <= level.getMinY() + 4) {
            return false;
        }
        float radius = Math.min(5.0F, 1.2F + height / 7.0F);
        float leanX = (random.nextFloat() - 0.5F) * 0.5F;
        float leanZ = (random.nextFloat() - 0.5F) * 0.5F;
        BlockState packed = Blocks.PACKED_ICE.defaultBlockState();
        BlockState blue = Blocks.BLUE_ICE.defaultBlockState();
        BlockState clear = Blocks.ICE.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int r0 = Mth.ceil(radius);
        for (int y = -3; y <= height; y++) {
            float t = Math.max(0.0F, y) / (float)height;
            float r = radius * (float)Math.pow(1.0F - t, 1.35F) + 0.35F;
            float cx = leanX * Math.max(0, y);
            float cz = leanZ * Math.max(0, y);
            for (int dx = -r0 - 1; dx <= r0 + 1; dx++) {
                for (int dz = -r0 - 1; dz <= r0 + 1; dz++) {
                    float fx = dx - cx + (Math.round(cx) - cx);
                    float fz = dz - cz + (Math.round(cz) - cz);
                    float d = Mth.sqrt(fx * fx + fz * fz);
                    if (d > r) {
                        continue;
                    }
                    int x = origin.getX() + dx + Math.round(cx);
                    int z = origin.getZ() + dz + Math.round(cz);
                    pos.set(x, baseY + y, z);
                    BlockState state = d < r * 0.45F ? blue : random.nextFloat() < 0.12F ? clear : packed;
                    set(level, pos, state);
                    if (y == -3) {
                        anchor(level, pos, x, baseY - 3, z, packed, 10);
                    }
                }
            }
        }
        BlockPos tip = new BlockPos(origin.getX() + Math.round(leanX * height), baseY + height + 1, origin.getZ() + Math.round(leanZ * height));
        if (height > 14 && level.getBlockState(tip).isAir() && solid(level, tip.below())) {
            set(level, tip, TheabyssModBlocks.FROST_CRYSTAL_ORE.get().defaultBlockState());
        }
        return true;
    }

    private boolean foundation(WorldGenLevel level, RandomSource random, BlockPos center) {
        int height = size(random);
        int radius = 6 + random.nextInt(6);
        int lowest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;
        for (int a = 0; a < 8; a++) {
            double angle = a * Math.PI / 4.0;
            int y = ground(level, center.getX() + (int)(Math.cos(angle) * radius), center.getZ() + (int)(Math.sin(angle) * radius));
            lowest = Math.min(lowest, y);
            highest = Math.max(highest, y);
        }
        if (lowest <= level.getMinY() + 4 || highest - lowest > 18) {
            return false;
        }
        float[] wobble = new float[16];
        for (int i = 0; i < wobble.length; i++) {
            wobble[i] = 0.8F + random.nextFloat() * 0.4F;
        }
        int top = highest + height;
        float bulgePhase = random.nextFloat() * Mth.TWO_PI;
        float bulgeFreq = 0.18F + random.nextFloat() * 0.2F;
        float[] crown = new float[16];
        for (int i = 0; i < crown.length; i++) {
            crown[i] = random.nextFloat() * 4.0F;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int nextVein = lowest + 4 + random.nextInt(6);
        double veinStart = 0.0;
        double veinLength = 0.0;
        for (int y = lowest - 3; y <= top; y++) {
            float t = (y - lowest) / (float)(top - lowest);
            float profile = 1.0F - 0.25F * t + 0.1F * Mth.sin(y * bulgeFreq + bulgePhase) + 0.05F * Mth.sin(y * 0.61F + bulgePhase * 2.0F);
            if (y < lowest + 4) {
                profile += 0.2F * (lowest + 4 - y) / 4.0F;
            }
            boolean vein = false;
            if (y == nextVein) {
                vein = true;
                veinStart = random.nextDouble() * Math.PI * 2.0;
                veinLength = Math.PI * (0.5 + random.nextDouble() * 0.9);
                nextVein = y + 4 + random.nextInt(9);
            } else if (y == nextVein - 1 && random.nextFloat() < 0.3F) {
                vein = true;
            }
            for (int dx = -REACH; dx <= REACH; dx++) {
                for (int dz = -REACH; dz <= REACH; dz++) {
                    double angle = Math.atan2(dz, dx) + Math.PI;
                    int sector = (int)Math.floor(angle / (Math.PI * 2.0) * wobble.length) % wobble.length;
                    float r = radius * profile * wobble[sector];
                    if (dx * dx + dz * dz > r * r || y > top - crown[sector]) {
                        continue;
                    }
                    pos.set(center.getX() + dx, y, center.getZ() + dz);
                    boolean edge = dx * dx + dz * dz > (r - 1.5F) * (r - 1.5F);
                    double relative = (angle - veinStart + Math.PI * 4.0) % (Math.PI * 2.0);
                    boolean inVein = vein && edge && relative < veinLength;
                    BlockState state = inVein ? (random.nextFloat() < 0.35F ? Blocks.BLUE_ICE : Blocks.PACKED_ICE).defaultBlockState() : stone();
                    set(level, pos, state);
                }
            }
        }
        for (int dx = -REACH; dx <= REACH; dx++) {
            for (int dz = -REACH; dz <= REACH; dz++) {
                for (int y = top + 1; y > top - 6; y--) {
                    pos.set(center.getX() + dx, y, center.getZ() + dz);
                    if (level.getBlockState(pos).is(TheabyssModBlocks.DARK_STONE.get()) && level.getBlockState(pos.above()).isAir()) {
                        set(level, pos, snow());
                        break;
                    }
                }
            }
        }
        if (random.nextFloat() < 0.6F) {
            for (int i = 0; i < 4 + random.nextInt(5); i++) {
                double angle = random.nextDouble() * Math.PI * 2.0;
                int x = center.getX() + (int)(Math.cos(angle) * radius * 0.6);
                int z = center.getZ() + (int)(Math.sin(angle) * radius * 0.6);
                BlockPos crystal = new BlockPos(x, top + 1, z);
                if (level.getBlockState(crystal).isAir() && solid(level, crystal.below())) {
                    set(level, crystal, TheabyssModBlocks.FROST_CRYSTAL_ORE.get().defaultBlockState());
                }
            }
        }
        return true;
    }

    private boolean arch(WorldGenLevel level, RandomSource random, BlockPos center) {
        int span = size(random);
        double angle = random.nextDouble() * Math.PI;
        double dirX = Math.cos(angle);
        double dirZ = Math.sin(angle);
        double half = Math.min(span / 2.0, REACH - 4);
        int ax = center.getX() - (int)Math.round(dirX * half);
        int az = center.getZ() - (int)Math.round(dirZ * half);
        int bx = center.getX() + (int)Math.round(dirX * half);
        int bz = center.getZ() + (int)Math.round(dirZ * half);
        int ga = ground(level, ax, az);
        int gb = ground(level, bx, bz);
        if (Math.min(ga, gb) <= level.getMinY() + 4 || Math.abs(ga - gb) > 14) {
            return false;
        }
        int rise = 9 + random.nextInt(Math.max(1, span / 2));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int steps = (int)(half * 6);
        for (int s = 0; s <= steps; s++) {
            double t = s / (double)steps;
            double x = Mth.lerp(t, ax, bx);
            double z = Mth.lerp(t, az, bz);
            double y = Mth.lerp(t, ga, gb) + Math.sin(Math.PI * t) * rise;
            double thickness = 1.7 + 1.9 * Math.pow(1.0 - Math.sin(Math.PI * t), 1.5);
            int r = Mth.ceil(thickness);
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (dx * dx + dy * dy + dz * dz > thickness * thickness) {
                            continue;
                        }
                        pos.set(Mth.floor(x) + dx, Mth.floor(y) + dy, Mth.floor(z) + dz);
                        set(level, pos, random.nextFloat() < 0.15F ? Blocks.PACKED_ICE.defaultBlockState() : stone());
                    }
                }
            }
            if (t < 0.12 || t > 0.88) {
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (dx * dx + dz * dz <= thickness * thickness) {
                            anchor(level, pos, Mth.floor(x) + dx, Mth.floor(y) - r, Mth.floor(z) + dz, stone(), 24);
                        }
                    }
                }
            }
        }
        dress(level, random, center, (int)half + 4, Math.max(ga, gb) + rise + 6, Math.min(ga, gb));
        return true;
    }

    private static void dress(WorldGenLevel level, RandomSource random, BlockPos center, int reach, int topY, int lowY) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState dark = stone();
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                for (int y = topY; y > lowY + 2; y--) {
                    pos.set(center.getX() + dx, y, center.getZ() + dz);
                    BlockState here = level.getBlockState(pos);
                    if (!here.is(dark.getBlock()) && !here.is(Blocks.PACKED_ICE)) {
                        continue;
                    }
                    BlockPos above = pos.above();
                    if (level.getBlockState(above).isAir()) {
                        set(level, pos, snow());
                    }
                    BlockPos below = pos.below();
                    if (level.getBlockState(below).isAir() && random.nextFloat() < 0.18F) {
                        int length = 1 + random.nextInt(3);
                        for (int i = 1; i <= length; i++) {
                            BlockPos drip = pos.below(i);
                            if (!level.getBlockState(drip).isAir()) {
                                break;
                            }
                            set(level, drip, i == length ? Blocks.ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState());
                        }
                    }
                }
            }
        }
    }
}
