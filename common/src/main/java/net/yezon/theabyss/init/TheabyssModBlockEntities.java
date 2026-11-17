package net.yezon.theabyss.init;

import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.block.wood.AbyssShelfBlockEntity;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.capabilities.RegisterCapabilitiesEvent;
import net.yezon.theabyss.platform.registries.DeferredBlock;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.platform.services.BlockItemHooks;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.block.entity.AbyssChestBlockEntity;
import net.yezon.theabyss.block.entity.ArcaneWorkbenchBlockEntity;
import net.yezon.theabyss.block.entity.CrystalCutterBlockEntity;
import net.yezon.theabyss.block.entity.MortarAndPestleBlockEntity;
import net.yezon.theabyss.block.entity.ResearchTableBlockEntity;
import net.yezon.theabyss.block.entity.SomniumExtractorBlockEntity;
import net.yezon.theabyss.block.entity.SomniumInfuserBlockEntity;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public class TheabyssModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TheabyssMod.MODID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SomniumExtractorBlockEntity>> SOMNIUM_EXTRACTOR = register(
        "somnium_extractor", TheabyssModBlocks.SOMNIUM_EXTRACTOR, SomniumExtractorBlockEntity::new
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcaneWorkbenchBlockEntity>> ARCANE_WORKBENCH = register(
        "arcane_workbench", TheabyssModBlocks.ARCANE_WORKBENCH, ArcaneWorkbenchBlockEntity::new
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResearchTableBlockEntity>> RESEARCH_TABLE = register(
        "research_table", TheabyssModBlocks.RESEARCH_TABLE, ResearchTableBlockEntity::new
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrystalCutterBlockEntity>> CRYSTAL_CUTTER = register(
        "crystal_cutter", TheabyssModBlocks.CRYSTAL_CUTTER, CrystalCutterBlockEntity::new
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SomniumInfuserBlockEntity>> SOMNIUM_INFUSER = register(
        "somnium_infuser", TheabyssModBlocks.SOMNIUM_INFUSER, SomniumInfuserBlockEntity::new
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<net.yezon.theabyss.block.entity.AbyssalEnergeticaBlockEntity>> ABYSSAL_ENERGETICA = register(
        "abyssal_energetica", TheabyssModBlocks.ABYSSAL_ENERGETICA, net.yezon.theabyss.block.entity.AbyssalEnergeticaBlockEntity::new
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MortarAndPestleBlockEntity>> MORTAR_AND_PESTLE = register(
        "mortar_and_pestle", TheabyssModBlocks.MORTAR_AND_PESTLE, MortarAndPestleBlockEntity::new
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<net.yezon.theabyss.block.entity.GravityStoneBlockEntity>> GRAVITY_STONE = register(
        "gravity_stone", TheabyssModBlocks.CHISELED_GRAVITY_STONE, net.yezon.theabyss.block.entity.GravityStoneBlockEntity::new
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AbyssChestBlockEntity>> ABYSS_CHEST = REGISTRY.register(
        "abyss_chest", () -> type(AbyssChestBlockEntity::new, TheabyssModChests.blocks())
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AbyssShelfBlockEntity>> SHELF = REGISTRY.register(
        "shelf", () -> type(AbyssShelfBlockEntity::new, TheabyssModWoodSets.shelves())
    );

    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(
        String name, DeferredBlock<Block> block, BiFunction<BlockPos, BlockState, T> supplier
    ) {
        return REGISTRY.register(name, () -> type(supplier, block.get()));
    }

    private static <T extends BlockEntity> BlockEntityType<T> type(BiFunction<BlockPos, BlockState, ? extends T> factory, Block... blocks) {
        return BlockItemHooks.get().blockEntityType(factory, blocks);
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerSidedItemContainer(SOMNIUM_EXTRACTOR.get());
        event.registerSidedItemContainer(ARCANE_WORKBENCH.get());
        event.registerSidedItemContainer(RESEARCH_TABLE.get());
        event.registerSidedItemContainer(CRYSTAL_CUTTER.get());
        event.registerSidedItemContainer(SOMNIUM_INFUSER.get());
        event.registerSidedItemContainer(MORTAR_AND_PESTLE.get());
        event.registerItemContainer(SHELF.get());
        event.registerChestItemContainer(TheabyssModChests.blocks());
    }
}
