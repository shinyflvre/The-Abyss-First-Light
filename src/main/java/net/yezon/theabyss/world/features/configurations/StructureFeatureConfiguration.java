package net.yezon.theabyss.world.features.configurations;

import java.util.Optional;
import net.yezon.theabyss.world.features.HangingPlants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public record StructureFeatureConfiguration(
    Identifier structure, boolean randomRotation, boolean randomMirror, HolderSet<Block> ignoredBlocks, Vec3i offset, boolean centered, boolean requireSpace,
    Optional<HangingPlants> hanging
) {
    public static final MapCodec<StructureFeatureConfiguration> CODEC = RecordCodecBuilder.mapCodec(
        builder -> builder.group(
                Identifier.CODEC.fieldOf("structure").forGetter(StructureFeatureConfiguration::structure),
                Codec.BOOL.fieldOf("random_rotation").orElse(false).forGetter(StructureFeatureConfiguration::randomRotation),
                Codec.BOOL.fieldOf("random_mirror").orElse(false).forGetter(StructureFeatureConfiguration::randomMirror),
                RegistryCodecs.holderSet(Registries.BLOCK).fieldOf("ignored_blocks").forGetter(StructureFeatureConfiguration::ignoredBlocks),
                Vec3i.offsetCodec(48).optionalFieldOf("offset", Vec3i.ZERO).forGetter(StructureFeatureConfiguration::offset),
                Codec.BOOL.optionalFieldOf("centered", false).forGetter(StructureFeatureConfiguration::centered),
                Codec.BOOL.optionalFieldOf("require_space", false).forGetter(StructureFeatureConfiguration::requireSpace),
                HangingPlants.CODEC.optionalFieldOf("hanging").forGetter(StructureFeatureConfiguration::hanging)
            )
            .apply(builder, StructureFeatureConfiguration::new)
    );
}
