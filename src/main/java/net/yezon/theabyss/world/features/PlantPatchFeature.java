package net.yezon.theabyss.world.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.block.flora.HangingPlantBodyBlock;
import net.yezon.theabyss.block.flora.HangingPlantHeadBlock;

public record PlantPatchFeature(Block plant, IntProvider count, int spread, IntProvider height) implements Feature {
    public static final MapCodec<PlantPatchFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        BuiltInRegistries.BLOCK.byNameCodec().fieldOf("plant").forGetter(PlantPatchFeature::plant),
        IntProviders.codec(1, 32).fieldOf("count").forGetter(PlantPatchFeature::count),
        Codec.intRange(0, 8).fieldOf("spread").forGetter(PlantPatchFeature::spread),
        IntProviders.codec(1, 32).fieldOf("height").forGetter(PlantPatchFeature::height)
    ).apply(i, PlantPatchFeature::new));
    public static final DeferredRegister<MapCodec<? extends Feature>> REGISTRY = DeferredRegister.create(Registries.FEATURE_TYPE, TheabyssMod.MODID);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<PlantPatchFeature>> PLANT_PATCH = REGISTRY.register("plant_patch", () -> CODEC);

    @Override
    public MapCodec<PlantPatchFeature> codec() {
        return CODEC;
    }

    private static boolean free(BlockState state) {
        return state.isAir() || state.canBeReplaced() && state.getFluidState().isEmpty() && !(state.getBlock() instanceof DoublePlantBlock);
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (!(this.plant instanceof HangingPlantHeadBlock head) || head.growth() != Direction.UP) {
            return false;
        }
        BlockState tip = head.getMaxAgeState(head.defaultBlockState());
        BlockState body = head.body().defaultBlockState();
        int members = this.count.sample(random);
        boolean placed = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int m = 0; m < members; m++) {
            int x = origin.getX() + (m == 0 ? 0 : random.nextInt(2 * this.spread + 1) - this.spread);
            int z = origin.getZ() + (m == 0 ? 0 : random.nextInt(2 * this.spread + 1) - this.spread);
            BlockPos base = new BlockPos(x, level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z), z);
            BlockState ground = level.getBlockState(base.below());
            if (!free(level.getBlockState(base)) || !tip.canSurvive(level, base) || ground.is(BlockTags.LEAVES) || ground.is(BlockTags.LOGS)) {
                continue;
            }
            int wanted = this.height.sample(random);
            int length = 0;
            pos.set(base);
            while (length < wanted && level.isInsideBuildHeight(pos) && free(level.getBlockState(pos))) {
                length++;
                pos.move(Direction.UP);
            }
            for (int i = 0; i < length; i++) {
                pos.setWithOffset(base, 0, i, 0);
                level.setBlock(pos, i == length - 1 ? tip : body.setValue(HangingPlantBodyBlock.TOP, i == 0), Block.UPDATE_CLIENTS);
            }
            placed |= length > 0;
        }
        return placed;
    }
}
