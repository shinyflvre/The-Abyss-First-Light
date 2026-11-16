package net.yezon.theabyss.neoforge.blocks;

import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.yezon.theabyss.platform.capabilities.RegisterCapabilitiesEvent;
import net.yezon.theabyss.platform.common.tooltip.TooltipAppender;
import net.yezon.theabyss.platform.common.tooltip.TooltipLocation;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.event.RegisterTooltipAppendersEvent;

public final class NeoForgeBlockItemEvents {
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

    private NeoForgeBlockItemEvents() {
    }

    public static void register(IEventBus bus) {
        if (AbyssEventBus.hasListeners(RegisterCapabilitiesEvent.class)) {
            bus.addListener(NeoForgeBlockItemEvents::onRegisterCapabilities);
        }
        if (AbyssEventBus.hasListeners(RegisterTooltipAppendersEvent.class)) {
            bus.addListener(NeoForgeBlockItemEvents::onRegisterTooltipAppenders);
        }
    }

    private static void onRegisterCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        AbyssEventBus.post(new RegisterCapabilitiesEvent() {
            @Override
            public <BE extends BlockEntity & WorldlyContainer> void registerSidedItemContainer(BlockEntityType<BE> type) {
                event.registerBlockEntity(Capabilities.Item.BLOCK, type, WorldlyContainerWrapper::new);
            }

            @Override
            public void registerChestItemContainer(Block... blocks) {
                event.registerBlock(Capabilities.Item.BLOCK, (level, pos, state, entity, side) ->
                    ((ChestBlock)state.getBlock()).combine(state, level, pos, true).apply(CHEST_HANDLER), blocks);
            }
        });
    }

    private static void onRegisterTooltipAppenders(net.neoforged.neoforge.event.RegisterTooltipAppendersEvent event) {
        AbyssEventBus.post(new RegisterTooltipAppendersEvent() {
            @Override
            public void registerAppender(TooltipLocation location, TooltipAppender appender) {
                event.registerAppender(net.neoforged.neoforge.common.tooltip.TooltipLocation.valueOf(location.name()), appender::append);
            }
        });
    }
}
