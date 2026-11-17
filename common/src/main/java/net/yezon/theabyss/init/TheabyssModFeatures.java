package net.yezon.theabyss.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.world.features.AbyssFeature;
import net.yezon.theabyss.world.features.AurelCrystalOreFeatureFeature;
import net.yezon.theabyss.world.features.BlossomRankPatch;
import net.yezon.theabyss.world.features.CavernaCrystalOreFeatureFeature;
import net.yezon.theabyss.world.features.DripstoneSpires;
import net.yezon.theabyss.world.features.FrostFormation;
import net.yezon.theabyss.world.features.MagmaFissures;
import net.yezon.theabyss.world.features.SpeleothemClusterFeature;
import net.yezon.theabyss.world.features.SpeleothemFeature;
import net.yezon.theabyss.world.features.SurfaceChest;

@EventBusSubscriber
public class TheabyssModFeatures {
    public static final DeferredRegister<Feature<?>> REGISTRY = DeferredRegister.create(Registries.FEATURE, TheabyssMod.MODID);
    public static final DeferredHolder<Feature<?>, AbyssFeature<AurelCrystalOreFeatureFeature>> AUREL_CRYSTAL_ORE_FEATURE = REGISTRY.register(
        "aurel_crystal_ore_feature", () -> new AbyssFeature<>(AurelCrystalOreFeatureFeature.CODEC)
    );
    public static final DeferredHolder<Feature<?>, AbyssFeature<CavernaCrystalOreFeatureFeature>> CAVERNA_CRYSTAL_ORE_FEATURE = REGISTRY.register(
        "caverna_crystal_ore_feature", () -> new AbyssFeature<>(CavernaCrystalOreFeatureFeature.CODEC)
    );
    public static final DeferredHolder<Feature<?>, AbyssFeature<FrostFormation>> FROST_FORMATION = REGISTRY.register(
        "frost_formation", () -> new AbyssFeature<>(FrostFormation.CODEC)
    );

    public static final DeferredHolder<Feature<?>, AbyssFeature<MagmaFissures>> MAGMA_FISSURES = REGISTRY.register(
        "magma_fissures", () -> new AbyssFeature<>(MagmaFissures.CODEC)
    );

    public static final DeferredHolder<Feature<?>, AbyssFeature<DripstoneSpires>> DRIPSTONE_SPIRES = REGISTRY.register(
        "dripstone_spires", () -> new AbyssFeature<>(DripstoneSpires.CODEC)
    );

    public static final DeferredHolder<Feature<?>, AbyssFeature<SurfaceChest>> SURFACE_CHEST = REGISTRY.register(
        "surface_chest", () -> new AbyssFeature<>(SurfaceChest.CODEC)
    );

    public static final DeferredHolder<Feature<?>, AbyssFeature<BlossomRankPatch>> BLOSSOM_RANK_PATCH = REGISTRY.register(
        "blossom_rank_patch", () -> new AbyssFeature<>(BlossomRankPatch.CODEC)
    );

    public static final DeferredHolder<Feature<?>, AbyssFeature<SpeleothemFeature>> SPELEOTHEM = REGISTRY.register(
        "speleothem", () -> new AbyssFeature<>(SpeleothemFeature.CODEC)
    );

    public static final DeferredHolder<Feature<?>, AbyssFeature<SpeleothemClusterFeature>> SPELEOTHEM_CLUSTER = REGISTRY.register(
        "speleothem_cluster", () -> new AbyssFeature<>(SpeleothemClusterFeature.CODEC)
    );
}
