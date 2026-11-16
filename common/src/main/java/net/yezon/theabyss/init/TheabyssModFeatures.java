package net.yezon.theabyss.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.platform.registries.RegisterEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.world.dimension.SolidTerrainRule;
import net.yezon.theabyss.world.features.AurelCrystalOreFeatureFeature;
import net.yezon.theabyss.world.features.CavernaCrystalOreFeatureFeature;

@EventBusSubscriber
public class TheabyssModFeatures {
    public static final DeferredRegister<MapCodec<? extends Feature>> REGISTRY = DeferredRegister.create(Registries.FEATURE_TYPE, TheabyssMod.MODID);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<AurelCrystalOreFeatureFeature>> AUREL_CRYSTAL_ORE_FEATURE = REGISTRY.register(
        "aurel_crystal_ore_feature", () -> AurelCrystalOreFeatureFeature.CODEC
    );
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CavernaCrystalOreFeatureFeature>> CAVERNA_CRYSTAL_ORE_FEATURE = REGISTRY.register(
        "caverna_crystal_ore_feature", () -> CavernaCrystalOreFeatureFeature.CODEC
    );
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<net.yezon.theabyss.world.features.FrostFormation>> FROST_FORMATION = REGISTRY.register(
        "frost_formation", () -> net.yezon.theabyss.world.features.FrostFormation.CODEC
    );

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<net.yezon.theabyss.world.features.MagmaFissures>> MAGMA_FISSURES = REGISTRY.register(
        "magma_fissures", () -> net.yezon.theabyss.world.features.MagmaFissures.CODEC
    );

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<net.yezon.theabyss.world.features.DripstoneSpires>> DRIPSTONE_SPIRES = REGISTRY.register(
        "dripstone_spires", () -> net.yezon.theabyss.world.features.DripstoneSpires.CODEC
    );

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<net.yezon.theabyss.world.features.SurfaceChest>> SURFACE_CHEST = REGISTRY.register(
        "surface_chest", () -> net.yezon.theabyss.world.features.SurfaceChest.CODEC
    );

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<net.yezon.theabyss.world.features.BlossomRankPatch>> BLOSSOM_RANK_PATCH = REGISTRY.register(
        "blossom_rank_patch", () -> net.yezon.theabyss.world.features.BlossomRankPatch.CODEC
    );

    @SubscribeEvent
    public static void registerMaterialRuleTypes(RegisterEvent event) {
        event.register(Registries.MATERIAL_RULE_TYPE, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "solid_terrain"), () -> SolidTerrainRule.CODEC);
    }
}
