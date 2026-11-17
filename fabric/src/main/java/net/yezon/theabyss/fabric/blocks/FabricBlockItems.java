package net.yezon.theabyss.fabric.blocks;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.registry.FlattenableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.yezon.theabyss.block.hooks.LevelLoadListener;
import net.yezon.theabyss.init.TheabyssModBlocks;
import net.yezon.theabyss.item.AbyssFuels;
import net.yezon.theabyss.platform.capabilities.RegisterCapabilitiesEvent;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.services.BlockItemHooks;

public final class FabricBlockItems {
    private FabricBlockItems() {
    }

    public static void installHooks() {
        BlockItemHooks.install(new FabricBlockItemHooks());
    }

    public static void afterRegistration() {
        AbyssEventBus.post(new RegisterCapabilitiesEvent() {
            @Override
            public <BE extends BlockEntity & WorldlyContainer> void registerSidedItemContainer(BlockEntityType<BE> type) {
                ItemStorage.SIDED.registerForBlockEntity((entity, side) -> InventoryStorage.of(entity, side), type);
            }

            @Override
            public <BE extends BlockEntity & Container> void registerItemContainer(BlockEntityType<BE> type) {
                ItemStorage.SIDED.registerForBlockEntity((entity, side) -> InventoryStorage.of(entity, null), type);
            }

            @Override
            public void registerChestItemContainer(Block... blocks) {
                ItemStorage.SIDED.registerForBlocks((level, pos, state, entity, side) -> {
                    Container container = ChestBlock.getContainer((ChestBlock)state.getBlock(), state, level, pos, true);
                    return container == null ? null : InventoryStorage.of(container, null);
                }, blocks);
            }
        });
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof LevelLoadListener listener) {
                listener.onLoadedIntoLevel();
            }
        });
        Set<Item> fuels = new HashSet<>();
        AbyssFuels.forEach((item, ticks) -> {
            FuelRegistry.INSTANCE.add(item, ticks);
            fuels.add(item);
        });
        for (Item item : BuiltInRegistries.ITEM) {
            if (AbyssFuels.isModItem(item) && !fuels.contains(item)) {
                FuelRegistry.INSTANCE.remove(item);
            }
        }
        FlattenableBlockRegistry.register(TheabyssModBlocks.LUSH_ROOTED_DIRT.get(), Blocks.DIRT_PATH.defaultBlockState());
        FabricBlockTransformables.register();
    }
}
