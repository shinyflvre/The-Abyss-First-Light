package net.yezon.theabyss.world.features;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModBlocks;
import org.jspecify.annotations.Nullable;

public final class BiomePathProcessor extends StructureProcessor {
    public static final MapCodec<BiomePathProcessor> CODEC = MapCodec.unit(BiomePathProcessor::new);
    public static final DeferredRegister<StructureProcessorType<?>> REGISTRY = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, TheabyssMod.MODID);
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<BiomePathProcessor>> TYPE = REGISTRY.register("biome_path", () -> () -> CODEC);
    private static final ResourceKey<Biome> NIGHTGROVE = ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "nightgrove"));

    @Override
    public StructureTemplate.@Nullable StructureBlockInfo processBlock(LevelReader level, BlockPos targetPosition, BlockPos referencePos,
        StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.@Nullable StructureBlockInfo processedBlockInfo, StructurePlaceSettings settings) {
        if (processedBlockInfo == null || !processedBlockInfo.state().is(TheabyssModBlocks.INFECTED_PATH.get())
            || settings.getBoundingBox() != null && !settings.getBoundingBox().isInside(processedBlockInfo.pos())
            || !level.getBiome(processedBlockInfo.pos()).is(NIGHTGROVE)) {
            return processedBlockInfo;
        }
        return new StructureTemplate.StructureBlockInfo(processedBlockInfo.pos(), TheabyssModBlocks.NIGHTGROVE_PATH.get().defaultBlockState(),
            processedBlockInfo.nbt());
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return TYPE.get();
    }
}
