package net.yezon.theabyss.world.features;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.world.features.configurations.StructureFeatureConfiguration;

public record StructureFeature(StructureFeatureConfiguration config) implements AbyssFeature.Config {
    public static final MapCodec<StructureFeature> CODEC = StructureFeatureConfiguration.CODEC.xmap(StructureFeature::new, StructureFeature::config);
    public static final DeferredRegister<Feature<?>> REGISTRY = DeferredRegister.create(Registries.FEATURE, TheabyssMod.MODID);
    public static final DeferredHolder<Feature<?>, AbyssFeature<StructureFeature>> STRUCTURE_FEATURE = REGISTRY.register(
        "structure_feature", () -> new AbyssFeature<>(CODEC)
    );

    @Override
    public MapCodec<StructureFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        Rotation rotation = this.config.randomRotation() ? Rotation.getRandom(random) : Rotation.NONE;
        Mirror mirror = this.config.randomMirror() ? Mirror.values()[random.nextInt(2)] : Mirror.NONE;
        BlockPos placePos = origin.offset(this.config.offset());
        StructureTemplateManager structureManager = level.getLevel().getServer().getStructureManager();
        StructureTemplate template = structureManager.getOrCreate(this.config.structure());
        StructurePlaceSettings placeSettings = new StructurePlaceSettings()
            .setRotation(rotation)
            .setMirror(mirror)
            .setRandom(random)
            .setIgnoreEntities(false)
            .addProcessor(new BlockIgnoreProcessor(this.config.ignoredBlocks().stream().map(Holder::value).toList()));
        if (this.config.centered()) {
            BoundingBox box = template.getBoundingBox(placeSettings, BlockPos.ZERO);
            placePos = new BlockPos(origin.getX() - box.getCenter().getX(), placePos.getY(), origin.getZ() - box.getCenter().getZ());
        }
        BoundingBox placed = template.getBoundingBox(placeSettings, placePos);
        if (this.config.requireSpace() && !this.hasSpace(level, placed)) {
            return false;
        }
        template.placeInWorld(level, placePos, placePos, placeSettings, random, Block.UPDATE_INVISIBLE);
        this.config.hanging().ifPresent(h -> h.decorate(level, random,
            BlockPos.betweenClosed(placed.minX(), placed.minY(), placed.minZ(), placed.maxX(), placed.maxY(), placed.maxZ())));
        return true;
    }

    private boolean hasSpace(WorldGenLevel level, BoundingBox box) {
        for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
                return false;
            }
        }
        return true;
    }
}
