package net.yezon.theabyss.init;

import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
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
        "abyss_chest", () -> new BlockEntityType<>(AbyssChestBlockEntity::new, Set.of(TheabyssModChests.blocks()))
    );
    private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, ResourceHandler<ItemResource>> CHEST_HANDLER = new DoubleBlockCombiner.Combiner<>() {
        @Override
        public ResourceHandler<ItemResource> acceptDouble(ChestBlockEntity first, ChestBlockEntity second) {
            return new CombinedResourceHandler<>(VanillaContainerWrapper.of(first), VanillaContainerWrapper.of(second));
        }

        @Override
        public ResourceHandler<ItemResource> acceptSingle(ChestBlockEntity single) {
            return VanillaContainerWrapper.of(single);
        }

        @Override
        public ResourceHandler<ItemResource> acceptNone() {
            return null;
        }
    };

    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(
        String name, DeferredBlock<Block> block, BlockEntityType.BlockEntitySupplier<T> supplier
    ) {
        return REGISTRY.register(name, () -> new BlockEntityType<>(supplier, Set.of(block.get())));
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, SOMNIUM_EXTRACTOR.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ARCANE_WORKBENCH.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Item.BLOCK, RESEARCH_TABLE.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Item.BLOCK, CRYSTAL_CUTTER.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Item.BLOCK, SOMNIUM_INFUSER.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Item.BLOCK, MORTAR_AND_PESTLE.get(), WorldlyContainerWrapper::new);
        event.registerBlock(Capabilities.Item.BLOCK, (level, pos, state, entity, side) ->
            ((ChestBlock)state.getBlock()).combine(state, level, pos, true).apply(CHEST_HANDLER), TheabyssModChests.blocks());
    }
}
