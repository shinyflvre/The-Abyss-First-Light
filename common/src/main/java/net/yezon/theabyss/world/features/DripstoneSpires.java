package net.yezon.theabyss.world.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.yezon.theabyss.init.TheabyssModBlocks;

public record DripstoneSpires(String mode, int minHeight, int maxHeight, int rise, Optional<HolderSet<Block>> ground) implements AbyssFeature.Config {
    public static final MapCodec<DripstoneSpires> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.optionalFieldOf("mode", "ground").forGetter(DripstoneSpires::mode),
            Codec.INT.optionalFieldOf("min_height", 1).forGetter(DripstoneSpires::minHeight),
            Codec.INT.optionalFieldOf("max_height", 6).forGetter(DripstoneSpires::maxHeight),
            Codec.INT.optionalFieldOf("rise", 3).forGetter(DripstoneSpires::rise),
            RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("ground").forGetter(DripstoneSpires::ground))
        .apply(i, DripstoneSpires::new));
    private static final int PROBE = 3;

    @Override
    public MapCodec<DripstoneSpires> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        int x = origin.getX();
        int z = origin.getZ();
        int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        BlockState ground = level.getBlockState(new BlockPos(x, top, z));
        if (this.ground.isPresent() ? !ground.is(this.ground.get()) : !rock(ground)) {
            return false;
        }
        if ("rock".equals(this.mode)) {
            int lowest = top;
            for (int dx = -PROBE; dx <= PROBE; dx += PROBE) {
                for (int dz = -PROBE; dz <= PROBE; dz += PROBE) {
                    lowest = Math.min(lowest, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x + dx, z + dz) - 1);
                }
            }
            if (top - lowest < this.rise) {
                return false;
            }
        }
        int height = this.minHeight + random.nextInt(Math.max(1, this.maxHeight - this.minHeight + 1));
        return spire(level, x, top + 1, z, height) > 0;
    }

    static boolean rock(BlockState state) {
        return state.is(TheabyssModBlocks.STONE.get()) || state.is(TheabyssModBlocks.STONE_RACKLE.get()) || state.is(TheabyssModBlocks.UNSTABLE_OBSIDIAN.get())
            || state.is(TheabyssModBlocks.GRAVEL.get());
    }

    static int spire(WorldGenLevel level, int x, int y, int z, int height) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int room = 0;
        while (room < height && level.getBlockState(pos.set(x, y + room, z)).isAir()) {
            room++;
        }
        if (room == 0) {
            return 0;
        }
        BlockState base = TheabyssModBlocks.POINTED_ABYSS_DRIPSTONE.get().defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.UP);
        for (int i = 0; i < room; i++) {
            DripstoneThickness thickness;
            int fromTip = room - 1 - i;
            if (fromTip == 0) {
                thickness = DripstoneThickness.TIP;
            } else if (fromTip == 1) {
                thickness = DripstoneThickness.FRUSTUM;
            } else if (i == 0) {
                thickness = DripstoneThickness.BASE;
            } else {
                thickness = DripstoneThickness.MIDDLE;
            }
            level.setBlock(pos.set(x, y + i, z), base.setValue(PointedDripstoneBlock.THICKNESS, thickness), 2);
        }
        return room;
    }
}
