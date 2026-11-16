package net.yezon.theabyss.fabric.blocks;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.yezon.theabyss.block.hooks.LevelLoadListener;
import net.yezon.theabyss.platform.capabilities.RegisterCapabilitiesEvent;
import net.yezon.theabyss.platform.common.tooltip.TooltipAppender;
import net.yezon.theabyss.platform.common.tooltip.TooltipLocation;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.event.RegisterTooltipAppendersEvent;
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
                ItemStorage.SIDED.registerForBlockEntity((entity, side) -> ContainerStorage.of(entity, side), type);
            }

            @Override
            public void registerChestItemContainer(Block... blocks) {
                ItemStorage.SIDED.registerForBlocks((level, pos, state, entity, side) -> {
                    Container container = ChestBlock.getContainer((ChestBlock)state.getBlock(), state, level, pos, true);
                    return container == null ? null : ContainerStorage.of(container, null);
                }, blocks);
            }
        });
        AbyssEventBus.post(new RegisterTooltipAppendersEvent() {
            @Override
            public void registerAppender(TooltipLocation location, TooltipAppender appender) {
                FabricTooltipAppenders.add(location, appender);
            }
        });
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof LevelLoadListener listener) {
                listener.onLoadedIntoLevel();
            }
        });
        FabricBlockTransformables.register();
    }
}
