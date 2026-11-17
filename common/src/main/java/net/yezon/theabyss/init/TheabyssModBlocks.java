package net.yezon.theabyss.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.Block;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.RegisterColorHandlersEvent;
import net.yezon.theabyss.platform.registries.DeferredBlock;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.block.AberytheOreBlock;
import net.yezon.theabyss.block.AbyssCrystalOreBlock;
import net.yezon.theabyss.block.AnimaFireBlock;
import net.yezon.theabyss.block.ArcaneWorkbenchBlock;
import net.yezon.theabyss.block.ArenoBlock;
import net.yezon.theabyss.block.AurelCrystalOreBlock;
import net.yezon.theabyss.block.BlackVoidBlock;
import net.yezon.theabyss.block.BlaruButtonBlock;
import net.yezon.theabyss.block.BlaruDoorBlock;
import net.yezon.theabyss.block.BlaruFenceBlock;
import net.yezon.theabyss.block.BlaruFenceGateBlock;
import net.yezon.theabyss.block.BlaruGrassBlock;
import net.yezon.theabyss.block.BlaruLeaveCarpetBlock;
import net.yezon.theabyss.block.BlaruLeavesBlock;
import net.yezon.theabyss.block.BlaruLogBlock;
import net.yezon.theabyss.block.BlaruMossBlock;
import net.yezon.theabyss.block.BlaruPlanksBlock;
import net.yezon.theabyss.block.BlaruPressurePlateBlock;
import net.yezon.theabyss.block.BlaruSlabBlock;
import net.yezon.theabyss.block.BlaruStairsBlock;
import net.yezon.theabyss.block.BlaruTrapDoorBlock;
import net.yezon.theabyss.block.BlueCaveShroomBlock;
import net.yezon.theabyss.block.BogButtonBlock;
import net.yezon.theabyss.block.BogDoorBlock;
import net.yezon.theabyss.block.BogFenceBlock;
import net.yezon.theabyss.block.BogFenceGateBlock;
import net.yezon.theabyss.block.BogLogFungiBlock;
import net.yezon.theabyss.block.BogPlanksBlock;
import net.yezon.theabyss.block.BogPressurePlateBlock;
import net.yezon.theabyss.block.BogShroomLogBlock;
import net.yezon.theabyss.block.BogSlabBlock;
import net.yezon.theabyss.block.BogStairsBlock;
import net.yezon.theabyss.block.BogTrapDoorBlock;
import net.yezon.theabyss.block.CaecusVersaBlock;
import net.yezon.theabyss.block.CavernaCrystalOreBlock;
import net.yezon.theabyss.block.CobbleStoneBlock;
import net.yezon.theabyss.block.CobbleStoneSlabBlock;
import net.yezon.theabyss.block.CobbleStoneStairsBlock;
import net.yezon.theabyss.block.CobbleStoneWallBlock;
import net.yezon.theabyss.block.CrimsonCrystalOreBlock;
import net.yezon.theabyss.block.CrystalCutterBlock;
import net.yezon.theabyss.block.DarkStoneBlock;
import net.yezon.theabyss.block.DiamondVariantBlock;
import net.yezon.theabyss.block.DirtBlock;
import net.yezon.theabyss.block.EmeraldVariantBlock;
import net.yezon.theabyss.block.EnderCrystalOreBlock;
import net.yezon.theabyss.block.EnderHangingLanternBlock;
import net.yezon.theabyss.block.EnderLanternBlock;
import net.yezon.theabyss.block.ExoliusPlantBlock;
import net.yezon.theabyss.block.FlosVitaeBlock;
import net.yezon.theabyss.block.FortisUltimaBlock;
import net.yezon.theabyss.block.FrostCrystalOreBlock;
import net.yezon.theabyss.block.FrozenButtonBlock;
import net.yezon.theabyss.block.FrozenDoorBlock;
import net.yezon.theabyss.block.FrozenFenceBlock;
import net.yezon.theabyss.block.FrozenFenceGateBlock;
import net.yezon.theabyss.block.FrozenLeavesBlock;
import net.yezon.theabyss.block.FrozenLogBlock;
import net.yezon.theabyss.block.FrozenPlanksBlock;
import net.yezon.theabyss.block.FrozenPressurePlateBlock;
import net.yezon.theabyss.block.FrozenSlabBlock;
import net.yezon.theabyss.block.FrozenStairsBlock;
import net.yezon.theabyss.block.FrozenTrapDoorBlock;
import net.yezon.theabyss.block.FusionOreBlock;
import net.yezon.theabyss.block.GarniteOreBlock;
import net.yezon.theabyss.block.GlacerytheOreBlock;
import net.yezon.theabyss.block.GlowShroomBlock;
import net.yezon.theabyss.block.GlowingBlaruMossBlock;
import net.yezon.theabyss.block.ToxicSunFlowerBlock;
import net.yezon.theabyss.block.GoldVariantBlock;
import net.yezon.theabyss.block.HealCampFireBlock;
import net.yezon.theabyss.block.HoglinHangingLanternBlock;
import net.yezon.theabyss.block.HoglinLanternBlock;
import net.yezon.theabyss.block.HollowCrystalOreBlock;
import net.yezon.theabyss.block.IceRankBlock;
import net.yezon.theabyss.block.IcelaRankBlock;
import net.yezon.theabyss.block.IcoraFlowerBlock;
import net.yezon.theabyss.block.IgnisitheOreBlock;
import net.yezon.theabyss.block.IncorytheOreBlock;
import net.yezon.theabyss.block.InfectedBushBlock;
import net.yezon.theabyss.block.InfectedGrassBlock;
import net.yezon.theabyss.block.InfectedShortGrassBlock;
import net.yezon.theabyss.block.InfectedGravelBlock;
import net.yezon.theabyss.block.InfectedPeatBlock;
import net.yezon.theabyss.block.InfusedLavaBlock;
import net.yezon.theabyss.block.IronVariantBlock;
import net.yezon.theabyss.block.NightgroveBushBlock;
import net.yezon.theabyss.block.NightgroveFarmlandBlock;
import net.yezon.theabyss.block.NightgrovePathBlock;
import net.yezon.theabyss.block.InfectedFarmlandBlock;
import net.yezon.theabyss.block.InfectedPathBlock;
import net.yezon.theabyss.block.NightgroveButtonBlock;
import net.yezon.theabyss.block.NightgroveDirtBlock;
import net.yezon.theabyss.block.NightgroveDoorBlock;
import net.yezon.theabyss.block.NightgroveFenceBlock;
import net.yezon.theabyss.block.NightgroveFenceGateBlock;
import net.yezon.theabyss.block.NightgroveGrassBlock;
import net.yezon.theabyss.block.NightgroveLeaveCarpetBlock;
import net.yezon.theabyss.block.NightgroveLeavesBlock;
import net.yezon.theabyss.block.NightgroveLogBlock;
import net.yezon.theabyss.block.NightgroveMelonBlock;
import net.yezon.theabyss.block.NightgroveMossBlock;
import net.yezon.theabyss.block.NightgrovePlanksBlock;
import net.yezon.theabyss.block.NightgrovePressurePlateBlock;
import net.yezon.theabyss.block.NightgroveSlabBlock;
import net.yezon.theabyss.block.NightgroveStairsBlock;
import net.yezon.theabyss.block.NightgroveTrapDoorBlock;
import net.yezon.theabyss.block.LoranEndOreBlock;
import net.yezon.theabyss.block.LoranFlowerBlock;
import net.yezon.theabyss.block.LoranOreBlock;
import net.yezon.theabyss.block.LunaButtonBlock;
import net.yezon.theabyss.block.LunaDoorBlock;
import net.yezon.theabyss.block.LunaFenceBlock;
import net.yezon.theabyss.block.LunaFenceGateBlock;
import net.yezon.theabyss.block.LunaLogBlock;
import net.yezon.theabyss.block.LunaPlanksBlock;
import net.yezon.theabyss.block.LunaPressurePlateBlock;
import net.yezon.theabyss.block.LunaShroomBlock;
import net.yezon.theabyss.block.LunaShroomLeaveBlock;
import net.yezon.theabyss.block.LunaSlabBlock;
import net.yezon.theabyss.block.LunaStairsBlock;
import net.yezon.theabyss.block.LunaTrapDoorBlock;
import net.yezon.theabyss.block.LungoRankBlock;
import net.yezon.theabyss.block.MortarAndPestleBlock;
import net.yezon.theabyss.block.NightAltarActivatedBlock;
import net.yezon.theabyss.block.NightAltarBlock;
import net.yezon.theabyss.block.OldExoliusPlantBlock;
import net.yezon.theabyss.block.PhantomHangingLanternBlock;
import net.yezon.theabyss.block.PhantomLanternBlock;
import net.yezon.theabyss.block.BlueShroomlightBlock;
import net.yezon.theabyss.block.RalomPlantBlock;
import net.yezon.theabyss.block.RaptorEggBlock;
import net.yezon.theabyss.block.RenaButtonBlock;
import net.yezon.theabyss.block.RenaDoorBlock;
import net.yezon.theabyss.block.RenaFenceBlock;
import net.yezon.theabyss.block.RenaFenceGateBlock;
import net.yezon.theabyss.block.RenaLogBlock;
import net.yezon.theabyss.block.RenaPlanksBlock;
import net.yezon.theabyss.block.RenaPressurePlateBlock;
import net.yezon.theabyss.block.RenaShroomBlock;
import net.yezon.theabyss.block.RenaShroomLeaveBlock;
import net.yezon.theabyss.block.RenaSlabBlock;
import net.yezon.theabyss.block.RenaStairsBlock;
import net.yezon.theabyss.block.RenaTrapDoorBlock;
import net.yezon.theabyss.block.ResearchTableBlock;
import net.yezon.theabyss.block.RomaMudBlock;
import net.yezon.theabyss.block.RubShroomBlock;
import net.yezon.theabyss.block.RycilianMushroomBlock;
import net.yezon.theabyss.block.SalButtonBlock;
import net.yezon.theabyss.block.SalDoorBlock;
import net.yezon.theabyss.block.SalFenceBlock;
import net.yezon.theabyss.block.SalFenceGateBlock;
import net.yezon.theabyss.block.SalLogFungiBlock;
import net.yezon.theabyss.block.SalPlanksBlock;
import net.yezon.theabyss.block.SalPressurePlateBlock;
import net.yezon.theabyss.block.SalShroomLogBlock;
import net.yezon.theabyss.block.SalSlabBlock;
import net.yezon.theabyss.block.SalStairsBlock;
import net.yezon.theabyss.block.SalTrapDoorBlock;
import net.yezon.theabyss.block.SaturniaVersaBlock;
import net.yezon.theabyss.block.ShieldCampFireBlock;
import net.yezon.theabyss.block.ShroomlandBushBlock;
import net.yezon.theabyss.block.ShroomlandGrassBlock;
import net.yezon.theabyss.block.ShroomlandMossBlock;
import net.yezon.theabyss.block.TallInfectedGrassBlock;
import net.yezon.theabyss.block.TallShroomlandGrassBlock;
import net.yezon.theabyss.block.SlimeBlockBlock;
import net.yezon.theabyss.block.SlimedButtonBlock;
import net.yezon.theabyss.block.SlimedCarpetBlock;
import net.yezon.theabyss.block.SlimedDoorBlock;
import net.yezon.theabyss.block.SlimedFenceBlock;
import net.yezon.theabyss.block.SlimedFenceGateBlock;
import net.yezon.theabyss.block.SlimedGrassBlock;
import net.yezon.theabyss.block.SlimedLogBlock;
import net.yezon.theabyss.block.SlimedLogCrystalBlock;
import net.yezon.theabyss.block.SlimedLogFungiBlock;
import net.yezon.theabyss.block.SlimedMossBlock;
import net.yezon.theabyss.block.SlimedPlanksBlock;
import net.yezon.theabyss.block.SlimedPressurePlateBlock;
import net.yezon.theabyss.block.SlimedSlabBlock;
import net.yezon.theabyss.block.SlimedStairsBlock;
import net.yezon.theabyss.block.SlimedTrapDoorBlock;
import net.yezon.theabyss.block.SlimedVitaeBlock;
import net.yezon.theabyss.block.SnowBlockBlock;
import net.yezon.theabyss.block.SomniumExtractorBlock;
import net.yezon.theabyss.block.SomniumInfuserBlock;
import net.yezon.theabyss.block.SralaShroomBlock;
import net.yezon.theabyss.block.StoneBlock;
import net.yezon.theabyss.block.StoneBrickBlock;
import net.yezon.theabyss.block.StoneBrickBrokenBlock;
import net.yezon.theabyss.block.StoneBrickChiseledBlock;
import net.yezon.theabyss.block.StoneBrickPillarBlock;
import net.yezon.theabyss.block.StoneBrickPillarIgnitedBlock;
import net.yezon.theabyss.block.StoneBrickSlabBlock;
import net.yezon.theabyss.block.StoneBrickStairsBlock;
import net.yezon.theabyss.block.StoneBrickWallBlock;
import net.yezon.theabyss.block.StoneRackleBlock;
import net.yezon.theabyss.block.StoneSlabsBlock;
import net.yezon.theabyss.block.StoneStairsBlock;
import net.yezon.theabyss.block.StrippedBlaruLogBlock;
import net.yezon.theabyss.block.StrippedBogShroomLogBlock;
import net.yezon.theabyss.block.StrippedFrozenLogBlock;
import net.yezon.theabyss.block.StrippedNightgroveLogBlock;
import net.yezon.theabyss.block.StrippedLunaLogBlock;
import net.yezon.theabyss.block.StrippedRenaLogBlock;
import net.yezon.theabyss.block.StrippedSalShroomLogBlock;
import net.yezon.theabyss.block.StrippedSlimedLogBlock;
import net.yezon.theabyss.block.TallBlaruGrassBlock;
import net.yezon.theabyss.block.TallGlowShroomBlock;
import net.yezon.theabyss.block.TallNightgroveGrassBlock;
import net.yezon.theabyss.block.TallRubShroomBlock;
import net.yezon.theabyss.block.TeslaRankBlock;
import net.yezon.theabyss.block.TheAbyssDimPortalBlock;
import net.yezon.theabyss.block.TheabyssdimiceworldPortalBlock;
import net.yezon.theabyss.block.ThunderFlowerBlock;
import net.yezon.theabyss.block.UnstableObsidianBlock;
import net.yezon.theabyss.block.VigilantBlueGrassBlock;
import net.yezon.theabyss.block.VigilantBushBlock;
import net.yezon.theabyss.block.VigilantButtonBlock;
import net.yezon.theabyss.block.VigilantDoorBlock;
import net.yezon.theabyss.block.VigilantFenceBlock;
import net.yezon.theabyss.block.VigilantFenceGateBlock;
import net.yezon.theabyss.block.VigilantGrassBlock;
import net.yezon.theabyss.block.VigilantHangingFruitBlock;
import net.yezon.theabyss.block.VigilantLeavesBlock;
import net.yezon.theabyss.block.VigilantLogBlock;
import net.yezon.theabyss.block.VigilantMossBlock;
import net.yezon.theabyss.block.VigilantPlanksBlock;
import net.yezon.theabyss.block.VigilantPressurePlateBlock;
import net.yezon.theabyss.block.VigilantSlabBlock;
import net.yezon.theabyss.block.VigilantStairsBlock;
import net.yezon.theabyss.block.VigilantTrapDoorBlock;
import net.yezon.theabyss.block.WarpedCrystalOreBlock;
import net.yezon.theabyss.block.YoungExoliusPlantBlock;

public class TheabyssModBlocks {
    public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(TheabyssMod.MODID);
    public static final DeferredBlock<Block> THE_ABYSS_PORTAL = REGISTRY.registerBlock("the_abyss_portal", TheAbyssDimPortalBlock::new);
    public static final DeferredBlock<Block> FROST_WORLD_PORTAL = REGISTRY.registerBlock("frost_world_portal", TheabyssdimiceworldPortalBlock::new);
    public static final DeferredBlock<Block> PHANTOM_LANTERN = REGISTRY.registerBlock("phantom_lantern", PhantomLanternBlock::new);
    public static final DeferredBlock<Block> ENDER_LANTERN = REGISTRY.registerBlock("ender_lantern", EnderLanternBlock::new);
    public static final DeferredBlock<Block> HOGLIN_LANTERN = REGISTRY.registerBlock("hoglin_lantern", HoglinLanternBlock::new);
    public static final DeferredBlock<Block> HEAL_CAMP_FIRE = REGISTRY.registerBlock("heal_camp_fire", HealCampFireBlock::new);
    public static final DeferredBlock<Block> SHIELD_CAMP_FIRE = REGISTRY.registerBlock("shield_camp_fire", ShieldCampFireBlock::new);
    public static final DeferredBlock<Block> FUSION_ORE = REGISTRY.registerBlock("fusion_ore", FusionOreBlock::new);
    public static final DeferredBlock<Block> LORAN_ORE = REGISTRY.registerBlock("loran_ore", LoranOreBlock::new);
    public static final DeferredBlock<Block> GARNITE_ORE = REGISTRY.registerBlock("garnite_ore", GarniteOreBlock::new);
    public static final DeferredBlock<Block> IGNISITHE_ORE = REGISTRY.registerBlock("ignisithe_ore", IgnisitheOreBlock::new);
    public static final DeferredBlock<Block> GLACERYTHE_ORE = REGISTRY.registerBlock("glacerythe_ore", GlacerytheOreBlock::new);
    public static final DeferredBlock<Block> ABERYTHE_ORE = REGISTRY.registerBlock("aberythe_ore", AberytheOreBlock::new);
    public static final DeferredBlock<Block> INCORYTHE_ORE = REGISTRY.registerBlock("incorythe_ore", IncorytheOreBlock::new);
    public static final DeferredBlock<Block> DIAMOND_VARIANT = REGISTRY.registerBlock("diamond_variant", DiamondVariantBlock::new);
    public static final DeferredBlock<Block> GOLD_VARIANT = REGISTRY.registerBlock("gold_variant", GoldVariantBlock::new);
    public static final DeferredBlock<Block> IRON_VARIANT = REGISTRY.registerBlock("iron_variant", IronVariantBlock::new);
    public static final DeferredBlock<Block> EMERALD_VARIANT = REGISTRY.registerBlock("emerald_variant", EmeraldVariantBlock::new);
    public static final DeferredBlock<Block> ROMA_MUD = REGISTRY.registerBlock("roma_mud", RomaMudBlock::new);
    public static final DeferredBlock<Block> BLARU_LOG = REGISTRY.registerBlock("blaru_log", BlaruLogBlock::new);
    public static final DeferredBlock<Block> STRIPPED_BLARU_LOG = REGISTRY.registerBlock("stripped_blaru_log", StrippedBlaruLogBlock::new);
    public static final DeferredBlock<Block> BLARU_PLANKS = REGISTRY.registerBlock("blaru_planks", BlaruPlanksBlock::new);
    public static final DeferredBlock<Block> BLARU_STAIRS = REGISTRY.registerBlock("blaru_stairs", BlaruStairsBlock::new);
    public static final DeferredBlock<Block> BLARU_SLAB = REGISTRY.registerBlock("blaru_slab", BlaruSlabBlock::new);
    public static final DeferredBlock<Block> BLARU_TRAP_DOOR = REGISTRY.registerBlock("blaru_trap_door", BlaruTrapDoorBlock::new);
    public static final DeferredBlock<Block> BLARU_DOOR = REGISTRY.registerBlock("blaru_door", BlaruDoorBlock::new);
    public static final DeferredBlock<Block> BLARU_FENCE = REGISTRY.registerBlock("blaru_fence", BlaruFenceBlock::new);
    public static final DeferredBlock<Block> BLARU_FENCE_GATE = REGISTRY.registerBlock("blaru_fence_gate", BlaruFenceGateBlock::new);
    public static final DeferredBlock<Block> BLARU_PRESSURE_PLATE = REGISTRY.registerBlock("blaru_pressure_plate", BlaruPressurePlateBlock::new);
    public static final DeferredBlock<Block> BLARU_BUTTON = REGISTRY.registerBlock("blaru_button", BlaruButtonBlock::new);
    public static final DeferredBlock<Block> NIGHTGROVE_LOG = REGISTRY.registerBlock("nightgrove_log", NightgroveLogBlock::new);
    public static final DeferredBlock<Block> STRIPPED_NIGHTGROVE_LOG = REGISTRY.registerBlock("stripped_nightgrove_log", StrippedNightgroveLogBlock::new);
    public static final DeferredBlock<Block> NIGHTGROVE_PLANKS = REGISTRY.registerBlock("nightgrove_planks", NightgrovePlanksBlock::new);
    public static final DeferredBlock<Block> NIGHTGROVE_STAIRS = REGISTRY.registerBlock("nightgrove_stairs", NightgroveStairsBlock::new);
    public static final DeferredBlock<Block> NIGHTGROVE_SLAB = REGISTRY.registerBlock("nightgrove_slab", NightgroveSlabBlock::new);
    public static final DeferredBlock<Block> NIGHTGROVE_TRAP_DOOR = REGISTRY.registerBlock("nightgrove_trap_door", NightgroveTrapDoorBlock::new);
    public static final DeferredBlock<Block> NIGHTGROVE_DOOR = REGISTRY.registerBlock("nightgrove_door", NightgroveDoorBlock::new);
    public static final DeferredBlock<Block> NIGHTGROVE_FENCE = REGISTRY.registerBlock("nightgrove_fence", NightgroveFenceBlock::new);
    public static final DeferredBlock<Block> NIGHTGROVE_FENCE_GATE = REGISTRY.registerBlock("nightgrove_fence_gate", NightgroveFenceGateBlock::new);
    public static final DeferredBlock<Block> NIGHTGROVE_PRESSURE_PLATE = REGISTRY.registerBlock("nightgrove_pressure_plate", NightgrovePressurePlateBlock::new);
    public static final DeferredBlock<Block> NIGHTGROVE_BUTTON = REGISTRY.registerBlock("nightgrove_button", NightgroveButtonBlock::new);
    public static final DeferredBlock<Block> BOG_SHROOM_LOG = REGISTRY.registerBlock("bog_shroom_log", BogShroomLogBlock::new);
    public static final DeferredBlock<Block> STRIPPED_BOG_SHROOM_LOG = REGISTRY.registerBlock("stripped_bog_shroom_log", StrippedBogShroomLogBlock::new);
    public static final DeferredBlock<Block> BOG_PLANKS = REGISTRY.registerBlock("bog_planks", BogPlanksBlock::new);
    public static final DeferredBlock<Block> BOG_STAIRS = REGISTRY.registerBlock("bog_stairs", BogStairsBlock::new);
    public static final DeferredBlock<Block> BOG_SLAB = REGISTRY.registerBlock("bog_slab", BogSlabBlock::new);
    public static final DeferredBlock<Block> BOG_TRAP_DOOR = REGISTRY.registerBlock("bog_trap_door", BogTrapDoorBlock::new);
    public static final DeferredBlock<Block> BOG_DOOR = REGISTRY.registerBlock("bog_door", BogDoorBlock::new);
    public static final DeferredBlock<Block> BOG_FENCE = REGISTRY.registerBlock("bog_fence", BogFenceBlock::new);
    public static final DeferredBlock<Block> BOG_FENCE_GATE = REGISTRY.registerBlock("bog_fence_gate", BogFenceGateBlock::new);
    public static final DeferredBlock<Block> BOG_PRESSURE_PLATE = REGISTRY.registerBlock("bog_pressure_plate", BogPressurePlateBlock::new);
    public static final DeferredBlock<Block> BOG_BUTTON = REGISTRY.registerBlock("bog_button", BogButtonBlock::new);
    public static final DeferredBlock<Block> SAL_SHROOM_LOG = REGISTRY.registerBlock("sal_shroom_log", SalShroomLogBlock::new);
    public static final DeferredBlock<Block> STRIPPED_SAL_SHROOM_LOG = REGISTRY.registerBlock("stripped_sal_shroom_log", StrippedSalShroomLogBlock::new);
    public static final DeferredBlock<Block> SAL_PLANKS = REGISTRY.registerBlock("sal_planks", SalPlanksBlock::new);
    public static final DeferredBlock<Block> SAL_STAIRS = REGISTRY.registerBlock("sal_stairs", SalStairsBlock::new);
    public static final DeferredBlock<Block> SAL_SLAB = REGISTRY.registerBlock("sal_slab", SalSlabBlock::new);
    public static final DeferredBlock<Block> SAL_TRAP_DOOR = REGISTRY.registerBlock("sal_trap_door", SalTrapDoorBlock::new);
    public static final DeferredBlock<Block> SAL_DOOR = REGISTRY.registerBlock("sal_door", SalDoorBlock::new);
    public static final DeferredBlock<Block> SAL_FENCE = REGISTRY.registerBlock("sal_fence", SalFenceBlock::new);
    public static final DeferredBlock<Block> SAL_FENCE_GATE = REGISTRY.registerBlock("sal_fence_gate", SalFenceGateBlock::new);
    public static final DeferredBlock<Block> SAL_PRESSURE_PLATE = REGISTRY.registerBlock("sal_pressure_plate", SalPressurePlateBlock::new);
    public static final DeferredBlock<Block> SAL_BUTTON = REGISTRY.registerBlock("sal_button", SalButtonBlock::new);
    public static final DeferredBlock<Block> SLIMED_LOG = REGISTRY.registerBlock("slimed_log", SlimedLogBlock::new);
    public static final DeferredBlock<Block> STRIPPED_SLIMED_LOG = REGISTRY.registerBlock("stripped_slimed_log", StrippedSlimedLogBlock::new);
    public static final DeferredBlock<Block> SLIMED_PLANKS = REGISTRY.registerBlock("slimed_planks", SlimedPlanksBlock::new);
    public static final DeferredBlock<Block> SLIMED_STAIRS = REGISTRY.registerBlock("slimed_stairs", SlimedStairsBlock::new);
    public static final DeferredBlock<Block> SLIMED_SLAB = REGISTRY.registerBlock("slimed_slab", SlimedSlabBlock::new);
    public static final DeferredBlock<Block> SLIMED_TRAP_DOOR = REGISTRY.registerBlock("slimed_trap_door", SlimedTrapDoorBlock::new);
    public static final DeferredBlock<Block> SLIMED_DOOR = REGISTRY.registerBlock("slimed_door", SlimedDoorBlock::new);
    public static final DeferredBlock<Block> SLIMED_FENCE = REGISTRY.registerBlock("slimed_fence", SlimedFenceBlock::new);
    public static final DeferredBlock<Block> SLIMED_FENCE_GATE = REGISTRY.registerBlock("slimed_fence_gate", SlimedFenceGateBlock::new);
    public static final DeferredBlock<Block> SLIMED_PRESSURE_PLATE = REGISTRY.registerBlock("slimed_pressure_plate", SlimedPressurePlateBlock::new);
    public static final DeferredBlock<Block> SLIMED_BUTTON = REGISTRY.registerBlock("slimed_button", SlimedButtonBlock::new);
    public static final DeferredBlock<Block> FROZEN_LOG = REGISTRY.registerBlock("frozen_log", FrozenLogBlock::new);
    public static final DeferredBlock<Block> STRIPPED_FROZEN_LOG = REGISTRY.registerBlock("stripped_frozen_log", StrippedFrozenLogBlock::new);
    public static final DeferredBlock<Block> FROZEN_PLANKS = REGISTRY.registerBlock("frozen_planks", FrozenPlanksBlock::new);
    public static final DeferredBlock<Block> FROZEN_STAIRS = REGISTRY.registerBlock("frozen_stairs", FrozenStairsBlock::new);
    public static final DeferredBlock<Block> FROZEN_SLAB = REGISTRY.registerBlock("frozen_slab", FrozenSlabBlock::new);
    public static final DeferredBlock<Block> FROZEN_TRAP_DOOR = REGISTRY.registerBlock("frozen_trap_door", FrozenTrapDoorBlock::new);
    public static final DeferredBlock<Block> FROZEN_DOOR = REGISTRY.registerBlock("frozen_door", FrozenDoorBlock::new);
    public static final DeferredBlock<Block> FROZEN_FENCE = REGISTRY.registerBlock("frozen_fence", FrozenFenceBlock::new);
    public static final DeferredBlock<Block> FROZEN_FENCE_GATE = REGISTRY.registerBlock("frozen_fence_gate", FrozenFenceGateBlock::new);
    public static final DeferredBlock<Block> FROZEN_PRESSURE_PLATE = REGISTRY.registerBlock("frozen_pressure_plate", FrozenPressurePlateBlock::new);
    public static final DeferredBlock<Block> FROZEN_BUTTON = REGISTRY.registerBlock("frozen_button", FrozenButtonBlock::new);
    public static final DeferredBlock<Block> RENA_LOG = REGISTRY.registerBlock("rena_log", RenaLogBlock::new);
    public static final DeferredBlock<Block> STRIPPED_RENA_LOG = REGISTRY.registerBlock("stripped_rena_log", StrippedRenaLogBlock::new);
    public static final DeferredBlock<Block> RENA_WOOD = REGISTRY.registerBlock("rena_wood", p -> new net.minecraft.world.level.block.RotatedPillarBlock(p.ignitedByLava().instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASS).sound(net.minecraft.world.level.block.SoundType.STEM).strength(2.0F, 9.5F)));
    public static final DeferredBlock<Block> STRIPPED_RENA_WOOD = REGISTRY.registerBlock("stripped_rena_wood", p -> new net.minecraft.world.level.block.RotatedPillarBlock(p.ignitedByLava().instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASS).sound(net.minecraft.world.level.block.SoundType.STEM).strength(2.0F, 9.5F)));
    public static final DeferredBlock<Block> RENA_PLANKS = REGISTRY.registerBlock("rena_planks", RenaPlanksBlock::new);
    public static final DeferredBlock<Block> RENA_STAIRS = REGISTRY.registerBlock("rena_stairs", RenaStairsBlock::new);
    public static final DeferredBlock<Block> RENA_SLAB = REGISTRY.registerBlock("rena_slab", RenaSlabBlock::new);
    public static final DeferredBlock<Block> RENA_TRAP_DOOR = REGISTRY.registerBlock("rena_trap_door", RenaTrapDoorBlock::new);
    public static final DeferredBlock<Block> RENA_DOOR = REGISTRY.registerBlock("rena_door", RenaDoorBlock::new);
    public static final DeferredBlock<Block> RENA_FENCE = REGISTRY.registerBlock("rena_fence", RenaFenceBlock::new);
    public static final DeferredBlock<Block> RENA_FENCE_GATE = REGISTRY.registerBlock("rena_fence_gate", RenaFenceGateBlock::new);
    public static final DeferredBlock<Block> RENA_PRESSURE_PLATE = REGISTRY.registerBlock("rena_pressure_plate", RenaPressurePlateBlock::new);
    public static final DeferredBlock<Block> RENA_BUTTON = REGISTRY.registerBlock("rena_button", RenaButtonBlock::new);
    public static final DeferredBlock<Block> LUNA_LOG = REGISTRY.registerBlock("luna_log", LunaLogBlock::new);
    public static final DeferredBlock<Block> STRIPPED_LUNA_LOG = REGISTRY.registerBlock("stripped_luna_log", StrippedLunaLogBlock::new);
    public static final DeferredBlock<Block> LUNA_WOOD = REGISTRY.registerBlock("luna_wood", p -> new net.minecraft.world.level.block.RotatedPillarBlock(p.ignitedByLava().instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASS).sound(net.minecraft.world.level.block.SoundType.STEM).strength(2.0F, 9.5F)));
    public static final DeferredBlock<Block> STRIPPED_LUNA_WOOD = REGISTRY.registerBlock("stripped_luna_wood", p -> new net.minecraft.world.level.block.RotatedPillarBlock(p.ignitedByLava().instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASS).sound(net.minecraft.world.level.block.SoundType.STEM).strength(2.0F, 9.5F)));
    public static final DeferredBlock<Block> LUNA_PLANKS = REGISTRY.registerBlock("luna_planks", LunaPlanksBlock::new);
    public static final DeferredBlock<Block> LUNA_STAIRS = REGISTRY.registerBlock("luna_stairs", LunaStairsBlock::new);
    public static final DeferredBlock<Block> LUNA_SLAB = REGISTRY.registerBlock("luna_slab", LunaSlabBlock::new);
    public static final DeferredBlock<Block> LUNA_TRAP_DOOR = REGISTRY.registerBlock("luna_trap_door", LunaTrapDoorBlock::new);
    public static final DeferredBlock<Block> LUNA_DOOR = REGISTRY.registerBlock("luna_door", LunaDoorBlock::new);
    public static final DeferredBlock<Block> LUNA_FENCE = REGISTRY.registerBlock("luna_fence", LunaFenceBlock::new);
    public static final DeferredBlock<Block> LUNA_FENCE_GATE = REGISTRY.registerBlock("luna_fence_gate", LunaFenceGateBlock::new);
    public static final DeferredBlock<Block> LUNA_PRESSURE_PLATE = REGISTRY.registerBlock("luna_pressure_plate", LunaPressurePlateBlock::new);
    public static final DeferredBlock<Block> LUNA_BUTTON = REGISTRY.registerBlock("luna_button", LunaButtonBlock::new);
    public static final DeferredBlock<Block> VIGILANT_LOG = REGISTRY.registerBlock("vigilant_log", VigilantLogBlock::new);
    public static final DeferredBlock<Block> VIGILANT_PLANKS = REGISTRY.registerBlock("vigilant_planks", VigilantPlanksBlock::new);
    public static final DeferredBlock<Block> VIGILANT_STAIRS = REGISTRY.registerBlock("vigilant_stairs", VigilantStairsBlock::new);
    public static final DeferredBlock<Block> VIGILANT_SLAB = REGISTRY.registerBlock("vigilant_slab", VigilantSlabBlock::new);
    public static final DeferredBlock<Block> VIGILANT_TRAP_DOOR = REGISTRY.registerBlock("vigilant_trap_door", VigilantTrapDoorBlock::new);
    public static final DeferredBlock<Block> VIGILANT_DOOR = REGISTRY.registerBlock("vigilant_door", VigilantDoorBlock::new);
    public static final DeferredBlock<Block> VIGILANT_FENCE = REGISTRY.registerBlock("vigilant_fence", VigilantFenceBlock::new);
    public static final DeferredBlock<Block> VIGILANT_FENCE_GATE = REGISTRY.registerBlock("vigilant_fence_gate", VigilantFenceGateBlock::new);
    public static final DeferredBlock<Block> VIGILANT_PRESSURE_PLATE = REGISTRY.registerBlock("vigilant_pressure_plate", VigilantPressurePlateBlock::new);
    public static final DeferredBlock<Block> VIGILANT_BUTTON = REGISTRY.registerBlock("vigilant_button", VigilantButtonBlock::new);
    public static final DeferredBlock<Block> BLARU_LEAVES = REGISTRY.registerBlock("blaru_leaves", BlaruLeavesBlock::new);
    public static final DeferredBlock<Block> NIGHTGROVE_LEAVES = REGISTRY.registerBlock("nightgrove_leaves", NightgroveLeavesBlock::new);
    public static final DeferredBlock<Block> FROZEN_LEAVES = REGISTRY.registerBlock("frozen_leaves", FrozenLeavesBlock::new);
    public static final DeferredBlock<Block> SLIME_BLOCK = REGISTRY.registerBlock("slime_block", SlimeBlockBlock::new);
    public static final DeferredBlock<Block> NIGHTGROVE_MELON = REGISTRY.registerBlock("nightgrove_melon", NightgroveMelonBlock::new);
    public static final DeferredBlock<Block> UNSTABLE_OBSIDIAN = REGISTRY.registerBlock("unstable_obsidian", UnstableObsidianBlock::new);
    public static final DeferredBlock<Block> STONE = REGISTRY.registerBlock("stone", StoneBlock::new);
    public static final DeferredBlock<Block> STONE_STAIRS = REGISTRY.registerBlock("stone_stairs", StoneStairsBlock::new);
    public static final DeferredBlock<Block> STONE_SLAB = REGISTRY.registerBlock("stone_slab", StoneSlabsBlock::new);
    public static final DeferredBlock<Block> COBBLE_STONE_STAIRS = REGISTRY.registerBlock("cobble_stone_stairs", CobbleStoneStairsBlock::new);
    public static final DeferredBlock<Block> COBBLE_STONE_SLAB = REGISTRY.registerBlock("cobble_stone_slab", CobbleStoneSlabBlock::new);
    public static final DeferredBlock<Block> COBBLE_STONE_WALL = REGISTRY.registerBlock("cobble_stone_wall", CobbleStoneWallBlock::new);
    public static final DeferredBlock<Block> COBBLE_STONE = REGISTRY.registerBlock("cobble_stone", CobbleStoneBlock::new);
    public static final DeferredBlock<Block> STONE_BRICK_STAIRS = REGISTRY.registerBlock("stone_brick_stairs", StoneBrickStairsBlock::new);
    public static final DeferredBlock<Block> STONE_BRICK = REGISTRY.registerBlock("stone_brick", StoneBrickBlock::new);
    public static final DeferredBlock<Block> STONE_BRICK_BROKEN = REGISTRY.registerBlock("stone_brick_broken", StoneBrickBrokenBlock::new);
    public static final DeferredBlock<Block> STONE_BRICK_CHISELED = REGISTRY.registerBlock("stone_brick_chiseled", StoneBrickChiseledBlock::new);
    public static final DeferredBlock<Block> CHISELED_GRAVITY_STONE = REGISTRY.registerBlock("chiseled_gravity_stone", net.yezon.theabyss.block.ChiseledGravityStoneBlock::new);
    public static final DeferredBlock<Block> STONE_BRICK_SLAB = REGISTRY.registerBlock("stone_brick_slab", StoneBrickSlabBlock::new);
    public static final DeferredBlock<Block> NIGHT_ALTAR = REGISTRY.registerBlock("night_altar", NightAltarBlock::new);
    public static final DeferredBlock<Block> NIGHT_ALTAR_ACTIVATED = REGISTRY.registerBlock("night_altar_activated", NightAltarActivatedBlock::new);
    public static final DeferredBlock<Block> STONE_BRICK_WALL = REGISTRY.registerBlock("stone_brick_wall", StoneBrickWallBlock::new);
    public static final DeferredBlock<Block> STONE_BRICK_PILLAR_IGNITED = REGISTRY.registerBlock("stone_brick_pillar_ignited", StoneBrickPillarIgnitedBlock::new);
    public static final DeferredBlock<Block> STONE_BRICK_PILLAR = REGISTRY.registerBlock("stone_brick_pillar", StoneBrickPillarBlock::new);
    public static final DeferredBlock<Block> BLUE_SHROOMLIGHT = REGISTRY.registerBlock("blue_shroomlight", BlueShroomlightBlock::new);
    public static final DeferredBlock<Block> DIRT = REGISTRY.registerBlock("dirt", DirtBlock::new);
    public static final DeferredBlock<Block> SOMNIUM_EXTRACTOR = REGISTRY.registerBlock("somnium_extractor", SomniumExtractorBlock::new);
    public static final DeferredBlock<Block> ARCANE_WORKBENCH = REGISTRY.registerBlock("arcane_workbench", ArcaneWorkbenchBlock::new);
    public static final DeferredBlock<Block> GAUNTLET_STATION = REGISTRY.registerBlock("gauntlet_station", net.yezon.theabyss.block.GauntletStationBlock::new);
    public static final DeferredBlock<Block> RESEARCH_TABLE = REGISTRY.registerBlock("research_table", ResearchTableBlock::new);
    public static final DeferredBlock<Block> CRYSTAL_CUTTER = REGISTRY.registerBlock("crystal_cutter", CrystalCutterBlock::new);
    public static final DeferredBlock<Block> SOMNIUM_INFUSER = REGISTRY.registerBlock("somnium_infuser", SomniumInfuserBlock::new);
    public static final DeferredBlock<Block> ABYSSAL_ENERGETICA = REGISTRY.registerBlock("abyssal_energetica", net.yezon.theabyss.block.AbyssalEnergeticaBlock::new);
    public static final DeferredBlock<Block> LUSH_MOSS_BLOCK = REGISTRY.registerBlock("lush_moss_block",
        p -> new net.yezon.theabyss.block.cave.BonemealFeatureBlock(net.yezon.theabyss.block.lush.LushCave.MOSS_PATCH_BONEMEAL, p),
        () -> BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.COLOR_CYAN).strength(0.1F).sound(net.minecraft.world.level.block.SoundType.MOSS).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
    public static final DeferredBlock<Block> LUSH_MOSS_CARPET = REGISTRY.registerBlock("lush_moss_carpet",
        net.minecraft.world.level.block.CarpetBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.COLOR_CYAN).strength(0.1F).sound(net.minecraft.world.level.block.SoundType.MOSS_CARPET).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
    public static final DeferredBlock<Block> LUSH_AZALEA = REGISTRY.registerBlock("lush_azalea",
        net.yezon.theabyss.block.lush.LushAzaleaBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).forceSolidOff().instabreak().sound(net.minecraft.world.level.block.SoundType.AZALEA).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
    public static final DeferredBlock<Block> LUSH_FLOWERING_AZALEA = REGISTRY.registerBlock("lush_flowering_azalea",
        net.yezon.theabyss.block.lush.LushAzaleaBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).forceSolidOff().instabreak().sound(net.minecraft.world.level.block.SoundType.FLOWERING_AZALEA).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
    public static final DeferredBlock<Block> LUSH_AZALEA_LEAVES = REGISTRY.registerBlock("lush_azalea_leaves",
        p -> new net.yezon.theabyss.block.lush.LushAzaleaLeavesBlock(0.01F, 0xFF16667A, p),
        () -> BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).strength(0.2F).randomTicks().sound(net.minecraft.world.level.block.SoundType.AZALEA_LEAVES).noOcclusion().isValidSpawn((s, g, p, e) -> false).isSuffocating((s, g, p) -> false).ignitedByLava().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY).isRedstoneConductor((s, g, p) -> false));
    public static final DeferredBlock<Block> LUSH_FLOWERING_AZALEA_LEAVES = REGISTRY.registerBlock("lush_flowering_azalea_leaves",
        p -> new net.yezon.theabyss.block.lush.LushAzaleaLeavesBlock(0.01F, 0xFF16667A, p),
        () -> BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).strength(0.2F).randomTicks().sound(net.minecraft.world.level.block.SoundType.AZALEA_LEAVES).noOcclusion().isValidSpawn((s, g, p, e) -> false).isSuffocating((s, g, p) -> false).ignitedByLava().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY).isRedstoneConductor((s, g, p) -> false));
    public static final DeferredBlock<Block> LUSH_CAVE_VINES = REGISTRY.registerBlock("lush_cave_vines",
        net.yezon.theabyss.block.lush.LushCaveVinesBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).randomTicks().noCollission().lightLevel(net.minecraft.world.level.block.CaveVines.emission(14)).instabreak().sound(net.minecraft.world.level.block.SoundType.CAVE_VINES).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
    public static final DeferredBlock<Block> LUSH_CAVE_VINES_PLANT = REGISTRY.registerBlock("lush_cave_vines_plant",
        net.yezon.theabyss.block.lush.LushCaveVinesPlantBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).noCollission().lightLevel(net.minecraft.world.level.block.CaveVines.emission(14)).instabreak().sound(net.minecraft.world.level.block.SoundType.CAVE_VINES).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
    public static final DeferredBlock<Block> LUSH_GLOW_LICHEN = REGISTRY.registerBlock("lush_glow_lichen",
        net.minecraft.world.level.block.GlowLichenBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.GLOW_LICHEN).replaceable().noCollission().strength(0.2F).sound(net.minecraft.world.level.block.SoundType.GLOW_LICHEN).lightLevel(net.minecraft.world.level.block.GlowLichenBlock.emission(7)).ignitedByLava().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
    public static final DeferredBlock<Block> LUSH_CLAY = REGISTRY.registerBlock("lush_clay",
        net.yezon.theabyss.block.lush.LushClayBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.CLAY).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.FLUTE).strength(0.6F).sound(net.minecraft.world.level.block.SoundType.GRAVEL));
    public static final DeferredBlock<Block> LUSH_SPORE_BLOSSOM = REGISTRY.registerBlock("lush_spore_blossom",
        net.yezon.theabyss.block.lush.LushSporeBlossomBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).instabreak().noCollission().sound(net.minecraft.world.level.block.SoundType.SPORE_BLOSSOM).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
    public static final DeferredBlock<Block> LUSH_BIG_DRIPLEAF = REGISTRY.registerBlock("lush_big_dripleaf",
        net.yezon.theabyss.block.lush.LushBigDripleafBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).forceSolidOff().strength(0.1F).sound(net.minecraft.world.level.block.SoundType.BIG_DRIPLEAF).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
    public static final DeferredBlock<Block> LUSH_BIG_DRIPLEAF_STEM = REGISTRY.registerBlock("lush_big_dripleaf_stem",
        net.yezon.theabyss.block.lush.LushBigDripleafStemBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).noCollission().strength(0.1F).sound(net.minecraft.world.level.block.SoundType.BIG_DRIPLEAF).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
    public static final DeferredBlock<Block> LUSH_SMALL_DRIPLEAF = REGISTRY.registerBlock("lush_small_dripleaf",
        net.yezon.theabyss.block.lush.LushSmallDripleafBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).noCollission().instabreak().sound(net.minecraft.world.level.block.SoundType.SMALL_DRIPLEAF).offsetType(BlockBehaviour.OffsetType.XYZ).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
    public static final DeferredBlock<Block> LUSH_HANGING_ROOTS = REGISTRY.registerBlock("lush_hanging_roots",
        net.yezon.theabyss.mixin.accessor.HangingRootsBlockInvoker::theabyss$create,
        () -> BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.DIRT).replaceable().noCollission().instabreak().sound(net.minecraft.world.level.block.SoundType.HANGING_ROOTS).offsetType(BlockBehaviour.OffsetType.XZ).ignitedByLava().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
    public static final DeferredBlock<Block> LUSH_ROOTED_DIRT = REGISTRY.registerBlock("lush_rooted_dirt",
        net.yezon.theabyss.block.lush.LushRootedDirtBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.DIRT).strength(0.5F).sound(net.minecraft.world.level.block.SoundType.ROOTED_DIRT));
    public static final DeferredBlock<Block> MORTAR_AND_PESTLE = REGISTRY.registerBlock("mortar_and_pestle", MortarAndPestleBlock::new);
    public static final DeferredBlock<Block> ARENO = REGISTRY.registerBlock("areno", ArenoBlock::new);
    public static final DeferredBlock<Block> INFECTED_PEAT = REGISTRY.registerBlock("infected_peat", InfectedPeatBlock::new);
    public static final DeferredBlock<Block> GRAVEL = REGISTRY.registerBlock("gravel", InfectedGravelBlock::new);
    public static final DeferredBlock<Block> NIGHTGROVE_MOSS = REGISTRY.registerBlock("nightgrove_moss", NightgroveMossBlock::new);
    public static final DeferredBlock<Block> NIGHTGROVE_DIRT = REGISTRY.registerBlock("nightgrove_dirt", NightgroveDirtBlock::new);
    public static final DeferredBlock<Block> INFECTED_GRASS = REGISTRY.registerBlock("infected_grass", InfectedGrassBlock::new);
    public static final DeferredBlock<Block> SLIMED_MOSS = REGISTRY.registerBlock("slimed_moss", SlimedMossBlock::new);
    public static final DeferredBlock<Block> SHROOMLAND_MOSS = REGISTRY.registerBlock("shroomland_moss", ShroomlandMossBlock::new);
    public static final DeferredBlock<Block> VIGILANT_MOSS = REGISTRY.registerBlock("vigilant_moss", VigilantMossBlock::new);
    public static final DeferredBlock<Block> BLARU_MOSS = REGISTRY.registerBlock("blaru_moss", BlaruMossBlock::new);
    public static final DeferredBlock<Block> STONE_RACKLE = REGISTRY.registerBlock("stone_rackle", StoneRackleBlock::new);
    public static final DeferredBlock<Block> RENA_SHROOM_LEAVE = REGISTRY.registerBlock("rena_shroom_leave", RenaShroomLeaveBlock::new);
    public static final DeferredBlock<Block> LUNA_SHROOM_LEAVE = REGISTRY.registerBlock("luna_shroom_leave", LunaShroomLeaveBlock::new);
    public static final DeferredBlock<Block> VIGILANT_LEAVES = REGISTRY.registerBlock("vigilant_leaves", VigilantLeavesBlock::new);
    public static final DeferredBlock<Block> LORAN_END_ORE = REGISTRY.registerBlock("loran_end_ore", LoranEndOreBlock::new);
    public static final DeferredBlock<Block> INFUSED_LAVA = REGISTRY.registerBlock("infused_lava", InfusedLavaBlock::new);
    public static final DeferredBlock<Block> DARK_STONE = REGISTRY.registerBlock("dark_stone", DarkStoneBlock::new);
    public static final DeferredBlock<Block> SNOW_BLOCK = REGISTRY.registerBlock("snow_block", SnowBlockBlock::new);
    public static final DeferredBlock<Block> BLACK_VOID = REGISTRY.registerBlock("black_void", BlackVoidBlock::new);
    public static final DeferredBlock<Block> LORAN_FLOWER = REGISTRY.registerBlock("loran_flower", LoranFlowerBlock::new);
    public static final DeferredBlock<Block> RALOM_PLANT = REGISTRY.registerBlock("ralom_plant", RalomPlantBlock::new);
    public static final DeferredBlock<Block> FORTIS_ULTIMA = REGISTRY.registerBlock("fortis_ultima", FortisUltimaBlock::new);
    public static final DeferredBlock<Block> THUNDER_FLOWER = REGISTRY.registerBlock("thunder_flower", ThunderFlowerBlock::new);
    public static final DeferredBlock<Block> SRALA_SHROOM = REGISTRY.registerBlock("srala_shroom", SralaShroomBlock::new);
    public static final DeferredBlock<Block> SATURNIA_VERSA = REGISTRY.registerBlock("saturnia_versa", SaturniaVersaBlock::new);
    public static final DeferredBlock<Block> CAECUS_VERSA = REGISTRY.registerBlock("caecus_versa", CaecusVersaBlock::new);
    public static final DeferredBlock<Block> FLOS_VITAE = REGISTRY.registerBlock("flos_vitae", FlosVitaeBlock::new);
    public static final DeferredBlock<Block> ANIMA_FIRE = REGISTRY.registerBlock("anima_fire", AnimaFireBlock::new);
    public static final DeferredBlock<Block> SLIMED_VITAE = REGISTRY.registerBlock("slimed_vitae", SlimedVitaeBlock::new);
    public static final DeferredBlock<Block> ICE_RANK = REGISTRY.registerBlock("ice_rank", IceRankBlock::new);
    public static final DeferredBlock<Block> ICORA_FLOWER = REGISTRY.registerBlock("icora_flower", IcoraFlowerBlock::new);
    public static final DeferredBlock<Block> TESLA_RANK = REGISTRY.registerBlock("tesla_rank", TeslaRankBlock::new);
    public static final DeferredBlock<Block> ICELA_RANK = REGISTRY.registerBlock("icela_rank", IcelaRankBlock::new);
    public static final DeferredBlock<Block> BLOSSOM_RANK = REGISTRY.registerBlock("blossom_rank", net.yezon.theabyss.block.BlossomRankBlock::new);
    public static final DeferredBlock<Block> BULB_RANK = REGISTRY.registerBlock("bulb_rank", net.yezon.theabyss.block.BulbRankBlock::new);
    public static final DeferredBlock<Block> TALL_GLOW_SHROOM = REGISTRY.registerBlock("tall_glow_shroom", TallGlowShroomBlock::new);
    public static final DeferredBlock<Block> GLOW_SHROOM = REGISTRY.registerBlock("glow_shroom", GlowShroomBlock::new);
    public static final DeferredBlock<Block> TALL_RUB_SHROOM = REGISTRY.registerBlock("tall_rub_shroom", TallRubShroomBlock::new);
    public static final DeferredBlock<Block> RUB_SHROOM = REGISTRY.registerBlock("rub_shroom", RubShroomBlock::new);
    public static final DeferredBlock<Block> SLIMED_LOG_FUNGI = REGISTRY.registerBlock("slimed_log_fungi", SlimedLogFungiBlock::new);
    public static final DeferredBlock<Block> BOG_LOG_FUNGI = REGISTRY.registerBlock("bog_log_fungi", BogLogFungiBlock::new);
    public static final DeferredBlock<Block> SAL_LOG_FUNGI = REGISTRY.registerBlock("sal_log_fungi", SalLogFungiBlock::new);
    public static final DeferredBlock<Block> SLIMED_LOG_CRYSTAL = REGISTRY.registerBlock("slimed_log_crystal", SlimedLogCrystalBlock::new);
    public static final DeferredBlock<Block> CRIMSON_CRYSTAL_ORE = REGISTRY.registerBlock("crimson_crystal_ore", CrimsonCrystalOreBlock::new);
    public static final DeferredBlock<Block> WARPED_CRYSTAL_ORE = REGISTRY.registerBlock("warped_crystal_ore", WarpedCrystalOreBlock::new);
    public static final DeferredBlock<Block> AUREL_CRYSTAL_ORE = REGISTRY.registerBlock("aurel_crystal_ore", AurelCrystalOreBlock::new);
    public static final DeferredBlock<Block> CAVERNA_CRYSTAL_ORE = REGISTRY.registerBlock("caverna_crystal_ore", CavernaCrystalOreBlock::new);
    public static final DeferredBlock<Block> ENDER_CRYSTAL_ORE = REGISTRY.registerBlock("ender_crystal_ore", EnderCrystalOreBlock::new);
    public static final DeferredBlock<Block> FROST_CRYSTAL_ORE = REGISTRY.registerBlock("frost_crystal_ore", FrostCrystalOreBlock::new);
    public static final DeferredBlock<Block> ABYSS_CRYSTAL_ORE = REGISTRY.registerBlock("abyss_crystal_ore", AbyssCrystalOreBlock::new);
    public static final DeferredBlock<Block> HOLLOW_CRYSTAL_ORE = REGISTRY.registerBlock("hollow_crystal_ore", HollowCrystalOreBlock::new);
    public static final DeferredBlock<Block> EXOLIUS_PLANT = REGISTRY.registerBlock("exolius_plant", ExoliusPlantBlock::new);
    public static final DeferredBlock<Block> YOUNG_EXOLIUS_PLANT = REGISTRY.registerBlock("young_exolius_plant", YoungExoliusPlantBlock::new);
    public static final DeferredBlock<Block> BLARU_LEAVE_CARPET = REGISTRY.registerBlock("blaru_leave_carpet", BlaruLeaveCarpetBlock::new);
    public static final DeferredBlock<Block> NIGHTGROVE_LEAVE_CARPET = REGISTRY.registerBlock("nightgrove_leave_carpet", NightgroveLeaveCarpetBlock::new);
    public static final DeferredBlock<Block> SLIMED_CARPET = REGISTRY.registerBlock("slimed_carpet", SlimedCarpetBlock::new);
    public static final DeferredBlock<Block> LUNGO_RANK = REGISTRY.registerBlock("lungo_rank", LungoRankBlock::new);
    public static final DeferredBlock<Block> GLOWING_BLARU_MOSS = REGISTRY.registerBlock("glowing_blaru_moss", GlowingBlaruMossBlock::new);
    public static final DeferredBlock<Block> VIGILANT_BUSH = REGISTRY.registerBlock("vigilant_bush", VigilantBushBlock::new);
    public static final DeferredBlock<Block> VIGILANT_HANGING_FRUIT = REGISTRY.registerBlock("vigilant_hanging_fruit", VigilantHangingFruitBlock::new);
    public static final DeferredBlock<Block> VIGILANT_GRASS = REGISTRY.registerBlock("vigilant_grass", VigilantGrassBlock::new);
    public static final DeferredBlock<Block> VIGILANT_BLUE_GRASS = REGISTRY.registerBlock("vigilant_blue_grass", VigilantBlueGrassBlock::new);
    public static final DeferredBlock<Block> BLARU_GRASS = REGISTRY.registerBlock("blaru_grass", BlaruGrassBlock::new);
    public static final DeferredBlock<Block> TALL_BLARU_GRASS = REGISTRY.registerBlock("tall_blaru_grass", TallBlaruGrassBlock::new);
    public static final DeferredBlock<Block> SLIMED_GRASS = REGISTRY.registerBlock("slimed_grass", SlimedGrassBlock::new);
    public static final DeferredBlock<Block> RYCILIAN_MUSHROOM = REGISTRY.registerBlock("rycilian_mushroom", RycilianMushroomBlock::new);
    public static final DeferredBlock<Block> OLD_EXOLIUS_PLANT = REGISTRY.registerBlock("old_exolius_plant", OldExoliusPlantBlock::new);
    public static final DeferredBlock<Block> NIGHTGROVE_GRASS = REGISTRY.registerBlock("nightgrove_grass", NightgroveGrassBlock::new);
    public static final DeferredBlock<Block> NIGHTGROVE_BUSH = REGISTRY.registerBlock("nightgrove_bush", NightgroveBushBlock::new);
    public static final DeferredBlock<Block> NIGHTGROVE_FARMLAND = REGISTRY.registerBlock("nightgrove_farmland", NightgroveFarmlandBlock::new);
    public static final DeferredBlock<Block> INFECTED_FARMLAND = REGISTRY.registerBlock("infected_farmland", InfectedFarmlandBlock::new);
    public static final DeferredBlock<Block> NIGHTGROVE_PATH = REGISTRY.registerBlock("nightgrove_path", NightgrovePathBlock::new);
    public static final DeferredBlock<Block> INFECTED_PATH = REGISTRY.registerBlock("infected_path", InfectedPathBlock::new);
    public static final DeferredBlock<Block> TALL_NIGHTGROVE_GRASS = REGISTRY.registerBlock("tall_nightgrove_grass", TallNightgroveGrassBlock::new);
    public static final DeferredBlock<Block> TOXIC_SUN_FLOWER = REGISTRY.registerBlock("toxic_sun_flower", ToxicSunFlowerBlock::new);

    static {
        REGISTRY.addAlias(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "glowing_sun_shroom"), ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "toxic_sun_flower"));
        REGISTRY.addAlias(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "purple_shroomlight"), ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "blue_shroomlight"));
    }
    public static final DeferredBlock<Block> BLUE_CAVE_SHROOM = REGISTRY.registerBlock("blue_cave_shroom", BlueCaveShroomBlock::new);
    public static final DeferredBlock<Block> RENA_SHROOM = REGISTRY.registerBlock("rena_shroom", RenaShroomBlock::new);
    public static final DeferredBlock<Block> TALL_RENA_SHROOM = REGISTRY.registerBlock("tall_rena_shroom", net.yezon.theabyss.block.TallShroomPlantBlock::new);
    public static final DeferredBlock<Block> LUNA_SHROOM = REGISTRY.registerBlock("luna_shroom", LunaShroomBlock::new);
    public static final DeferredBlock<Block> TALL_LUNA_SHROOM = REGISTRY.registerBlock("tall_luna_shroom", net.yezon.theabyss.block.TallShroomPlantBlock::new);
    public static final DeferredBlock<Block> SHROOMLAND_GRASS = REGISTRY.registerBlock("shroomland_grass", ShroomlandGrassBlock::new);
    public static final DeferredBlock<Block> TALL_SHROOMLAND_GRASS = REGISTRY.registerBlock("tall_shroomland_grass", TallShroomlandGrassBlock::new);
    public static final DeferredBlock<Block> SHROOMLAND_BUSH = REGISTRY.registerBlock("shroomland_bush", ShroomlandBushBlock::new);
    public static final DeferredBlock<Block> INFECTED_SHORT_GRASS = REGISTRY.registerBlock("infected_short_grass", InfectedShortGrassBlock::new);
    public static final DeferredBlock<Block> TALL_INFECTED_GRASS = REGISTRY.registerBlock("tall_infected_grass", TallInfectedGrassBlock::new);
    public static final DeferredBlock<Block> INFECTED_BUSH = REGISTRY.registerBlock("infected_bush", InfectedBushBlock::new);
    public static final DeferredBlock<Block> PHANTOM_HANGING_LANTERN = REGISTRY.registerBlock("phantom_hanging_lantern", PhantomHangingLanternBlock::new);
    public static final DeferredBlock<Block> ENDER_HANGING_LANTERN = REGISTRY.registerBlock("ender_hanging_lantern", EnderHangingLanternBlock::new);
    public static final DeferredBlock<Block> HOGLIN_HANGING_LANTERN = REGISTRY.registerBlock("hoglin_hanging_lantern", HoglinHangingLanternBlock::new);
    public static final DeferredBlock<Block> RAPTOR_EGG = REGISTRY.registerBlock("raptor_egg", RaptorEggBlock::new);
    public static final DeferredBlock<Block> SHADOW_PLATFORM = REGISTRY.registerBlock("shadow_platform", net.yezon.theabyss.block.ShadowPlatformBlock::new);
    public static final DeferredBlock<Block> DUSKVEIL_BLOSSOMS = REGISTRY.registerBlock("duskveil_blossoms", p -> net.yezon.theabyss.mixin.accessor.FlowerBedBlockInvoker.theabyss$create(p
        .mapColor(net.minecraft.world.level.material.MapColor.PLANT).noCollission().sound(net.minecraft.world.level.block.SoundType.PINK_PETALS)
        .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
    public static final DeferredBlock<Block> BLARU_STARPETALS = REGISTRY.registerBlock("blaru_starpetals", p -> net.yezon.theabyss.mixin.accessor.FlowerBedBlockInvoker.theabyss$create(p
        .mapColor(net.minecraft.world.level.material.MapColor.PLANT).noCollission().sound(net.minecraft.world.level.block.SoundType.PINK_PETALS)
        .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
    public static final DeferredBlock<Block> ABYSS_DRIPSTONE_BLOCK = REGISTRY.registerBlock("abyss_dripstone_block", p -> new Block(p
        .mapColor(net.minecraft.world.level.material.MapColor.TERRACOTTA_CYAN).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM)
        .sound(net.minecraft.world.level.block.SoundType.DRIPSTONE_BLOCK).requiresCorrectToolForDrops().strength(1.5F, 1.0F)));
    public static final DeferredBlock<Block> POINTED_ABYSS_DRIPSTONE = REGISTRY.registerBlock("pointed_abyss_dripstone",
        p -> new net.yezon.theabyss.block.cave.AbyssPointedDripstoneBlock(ABYSS_DRIPSTONE_BLOCK, p
            .mapColor(net.minecraft.world.level.material.MapColor.TERRACOTTA_CYAN).forceSolidOn()
            .instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).noOcclusion()
            .sound(net.minecraft.world.level.block.SoundType.POINTED_DRIPSTONE).randomTicks().strength(1.5F, 3.0F).dynamicShape()
            .offsetType(net.minecraft.world.level.block.state.BlockBehaviour.OffsetType.XZ)
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY).isRedstoneConductor((state, level, pos) -> false)));

    @EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
    public static class ClientSideHandler {
        @SubscribeEvent
        public static void blockColorLoad(RegisterColorHandlersEvent.Block event) {
            event.register((state, level, pos, tintIndex) -> tintIndex != 0 ? -1
                : level != null && pos != null ? BiomeColors.getAverageGrassColor(level, pos) : GrassColor.getDefaultColor(), INFECTED_GRASS.get());
            event.register((state, level, pos, tintIndex) -> tintIndex != 0 ? -1
                : level != null && pos != null ? BiomeColors.getAverageFoliageColor(level, pos) : -12012264,
                INFECTED_SHORT_GRASS.get(), TALL_INFECTED_GRASS.get(), INFECTED_BUSH.get());
        }
    }
}
