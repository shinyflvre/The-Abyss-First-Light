package net.yezon.theabyss.world.features;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModBlocks;
import org.jspecify.annotations.Nullable;

public record BiomePathProcessor() implements StructureProcessor {
    public static final MapCodec<BiomePathProcessor> CODEC = MapCodec.unit(BiomePathProcessor::new);
    public static final DeferredRegister<MapCodec<? extends StructureProcessor>> REGISTRY = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, TheabyssMod.MODID);
    public static final DeferredHolder<MapCodec<? extends StructureProcessor>, MapCodec<BiomePathProcessor>> TYPE = REGISTRY.register("biome_path", () -> CODEC);
    private static final ResourceKey<Biome> NIGHTGROVE = ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "nightgrove"));

    @Override
    public StructureTemplate.@Nullable StructureBlockInfo processBlock(LevelReader level, BlockPos targetPosition, BlockPos referencePos,
        BlockPos templateRelativePos, StructureTemplate.@Nullable StructureBlockInfo processedBlockInfo, StructurePlaceSettings settings) {
        if (processedBlockInfo == null || !processedBlockInfo.state().is(TheabyssModBlocks.INFECTED_PATH.get())
            || !level.getBiome(processedBlockInfo.pos()).is(NIGHTGROVE)) {
            return processedBlockInfo;
        }
        return new StructureTemplate.StructureBlockInfo(processedBlockInfo.pos(), TheabyssModBlocks.NIGHTGROVE_PATH.get().defaultBlockState(),
            processedBlockInfo.nbt());
    }

    @Override
    public MapCodec<? extends StructureProcessor> codec() {
        return CODEC;
    }
}
