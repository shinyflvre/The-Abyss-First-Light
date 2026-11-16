package net.yezon.theabyss.world.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;
import org.jspecify.annotations.Nullable;

public record BogTreeFeature(Variant variant, BlockState logState) implements Feature {
    public enum Variant implements StringRepresentable {
        TALL("tall"), LEANING("leaning"), FORKED("forked"), HOOKED("hooked");

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

    public static final MapCodec<BogTreeFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Variant.CODEC.fieldOf("variant").forGetter(BogTreeFeature::variant),
        BlockState.CODEC.fieldOf("log").forGetter(BogTreeFeature::logState)
    ).apply(i, BogTreeFeature::new));
    public static final DeferredRegister<MapCodec<? extends Feature>> REGISTRY = DeferredRegister.create(Registries.FEATURE_TYPE, TheabyssMod.MODID);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<BogTreeFeature>> BOG_TREE = REGISTRY.register("bog_tree", () -> CODEC);

    private static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[][] FOOT_SPOTS = {{2, 0}, {2, 1}, {-1, 0}, {-1, 1}, {0, 2}, {1, 2}, {0, -1}, {1, -1}};
    private static final int MAX_EXTEND = 6;

    @Override
    public MapCodec<BogTreeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        Shape shape = new Shape(random);
        switch (this.variant) {
            case TALL -> shape.tall();
            case LEANING -> shape.leaning();
            case FORKED -> shape.forked();
            case HOOKED -> shape.hooked();
        }
        int minDx = Integer.MAX_VALUE;
        int maxDx = Integer.MIN_VALUE;
        int minDz = Integer.MAX_VALUE;
        int maxDz = Integer.MIN_VALUE;
        for (long key : shape.blocks) {
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
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y - 1, z);
        while (pos.getY() > level.getMinY() && canReplace(level.getBlockState(pos))) {
            pos.move(Direction.DOWN);
        }
        y = pos.getY() + 1;
        BlockState ground = level.getBlockState(pos);
        if (ground.isAir() || !ground.getFluidState().isEmpty() || !ground.isFaceSturdy(level, pos, Direction.UP)) {
            return false;
        }
        for (long key : shape.blocks) {
            pos.set(x + BlockPos.getX(key), y + BlockPos.getY(key), z + BlockPos.getZ(key));
            BlockState here = level.getBlockState(pos);
            if (here.is(BlockTags.LOGS) || here.is(BlockTags.LEAVES)) {
                return false;
            }
        }
        for (long key : shape.blocks) {
            int dy = BlockPos.getY(key);
            pos.set(x + BlockPos.getX(key), y + dy, z + BlockPos.getZ(key));
            if (canReplace(level.getBlockState(pos))) {
                level.setBlock(pos, this.logState, Block.UPDATE_CLIENTS);
            }
            if (dy == 0) {
                for (int down = 1; down <= MAX_EXTEND; down++) {
                    pos.move(Direction.DOWN);
                    if (!canReplace(level.getBlockState(pos))) {
                        break;
                    }
                    level.setBlock(pos, this.logState, Block.UPDATE_CLIENTS);
                }
            }
        }
        return true;
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir() || state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    private static final class Shape {
        final Set<Long> blocks = new HashSet<>();
        final RandomSource rng;

        Shape(RandomSource rng) {
            this.rng = rng;
        }

        int randint(int a, int b) {
            return a + this.rng.nextInt(b - a + 1);
        }

        int[] dir() {
            return DIRS[this.rng.nextInt(DIRS.length)];
        }

        void put(int x, int y, int z) {
            this.blocks.add(BlockPos.asLong(x, y, z));
        }

        List<int[]> layer(int x, int y, int z, int w, boolean holes) {
            List<int[]> cells = new ArrayList<>();
            for (int dx = 0; dx < w; dx++) {
                for (int dz = 0; dz < w; dz++) {
                    cells.add(new int[]{x + dx, z + dz});
                }
            }
            if (w == 2 && holes && this.rng.nextDouble() < 0.28) {
                cells.remove(this.rng.nextInt(cells.size()));
            }
            for (int[] c : cells) {
                this.put(c[0], y, c[1]);
            }
            if (this.rng.nextDouble() < 0.22) {
                int[] c = cells.get(this.rng.nextInt(cells.size()));
                int[] d = this.dir();
                this.put(c[0] + d[0], y, c[1] + d[1]);
            }
            return cells;
        }

        void twig(int x, int y, int z) {
            int[] d = this.dir();
            int[] lengths = {1, 1, 2, 2, 3};
            int n = lengths[this.rng.nextInt(lengths.length)];
            int cx = x;
            int cy = y;
            int cz = z;
            for (int i = 0; i < n; i++) {
                cx += d[0];
                cz += d[1];
                if (i > 0 || this.rng.nextDouble() < 0.5) {
                    cy++;
                }
                this.put(cx, cy, cz);
            }
        }

        int[] column(int x, int z, int y0, int h, int thinFrom, int @Nullable [] lean, int shiftLo, int shiftHi, double twigs) {
            int y = y0;
            int next = this.randint(shiftLo, shiftHi);
            for (int i = 0; i < h; i++) {
                int w = y < thinFrom ? 2 : 1;
                List<int[]> cells = this.layer(x, y, z, w, y > y0 + 1);
                if (y > y0 + 2 && this.rng.nextDouble() < twigs) {
                    int[] c = cells.get(this.rng.nextInt(cells.size()));
                    this.twig(c[0], y, c[1]);
                }
                next--;
                if (next <= 0) {
                    int[] d = lean != null && this.rng.nextDouble() < 0.7 ? lean : this.dir();
                    x += d[0];
                    z += d[1];
                    next = this.randint(shiftLo, shiftHi);
                }
                y++;
            }
            return new int[]{x, y - 1, z};
        }

        void foot() {
            List<int[]> spots = new ArrayList<>(List.of(FOOT_SPOTS));
            Util.shuffle(spots, this.rng);
            int n = this.randint(2, 4);
            for (int i = 0; i < n; i++) {
                this.put(spots.get(i)[0], 0, spots.get(i)[1]);
            }
        }

        void crown(int x, int y, int z) {
            int n = this.randint(1, 3);
            for (int i = 0; i < n; i++) {
                int[] d = this.dir();
                x += d[0];
                z += d[1];
                if (this.rng.nextDouble() < 0.6) {
                    y++;
                }
                this.put(x, y, z);
                if (this.rng.nextDouble() < 0.4) {
                    this.put(x, y - 1, z);
                }
            }
        }

        void tall() {
            int h = this.randint(16, 22);
            int[] top = this.column(0, 0, 0, h, h - this.randint(3, 5), null, 2, 4, 0.14);
            this.crown(top[0], top[1], top[2]);
            this.foot();
        }

        void leaning() {
            int h = this.randint(14, 19);
            int thinFrom = h - this.randint(3, 6);
            int[] top = this.column(0, 0, 0, h, thinFrom, this.dir(), 1, 3, 0.14);
            this.crown(top[0], top[1], top[2]);
            this.foot();
        }

        void forked() {
            int hs = this.randint(7, 10);
            int[] top = this.column(0, 0, 0, hs, 99, null, 2, 4, 0.14);
            int[] d1 = this.dir();
            int[] d2 = {-d1[0], -d1[1]};
            for (int[] d : new int[][]{d1, d2}) {
                int sx = top[0] + (d[0] > 0 ? 1 : 0) + d[0];
                int sz = top[2] + (d[1] > 0 ? 1 : 0) + d[1];
                this.put(sx, top[1] + 1, sz);
                int h = this.randint(5, 9);
                int thinFrom = top[1] + 2 + this.randint(0, 2);
                int[] tip = this.column(sx, sz, top[1] + 2, h, thinFrom, d, 2, 3, 0.1);
                this.crown(tip[0], tip[1], tip[2]);
            }
            this.foot();
        }

        void hooked() {
            int h = this.randint(15, 20);
            int[] top = this.column(0, 0, 0, h, h - this.randint(2, 4), null, 3, 5, 0.14);
            int x = top[0];
            int y = top[1];
            int z = top[2];
            int[] d = this.dir();
            int across = this.randint(3, 5);
            for (int i = 0; i < across; i++) {
                x += d[0];
                z += d[1];
                this.put(x, y, z);
                if (this.rng.nextDouble() < 0.35) {
                    this.put(x, y + 1, z);
                }
            }
            int down = this.randint(2, 4);
            for (int i = 0; i < down; i++) {
                y--;
                this.put(x, y, z);
                if (this.rng.nextDouble() < 0.3) {
                    int[] dd = this.dir();
                    this.put(x + dd[0], y, z + dd[1]);
                }
            }
            this.foot();
        }
    }
}
