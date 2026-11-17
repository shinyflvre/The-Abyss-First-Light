package net.yezon.theabyss.neoforge.blocks;

import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.yezon.theabyss.init.TheabyssModBlocks;
import net.yezon.theabyss.item.AbyssFuels;
import net.yezon.theabyss.platform.capabilities.RegisterCapabilitiesEvent;
import net.yezon.theabyss.platform.event.AbyssEventBus;

public final class NeoForgeBlockItemEvents {
    private NeoForgeBlockItemEvents() {
    }

    public static void register(IEventBus bus) {
        if (AbyssEventBus.hasListeners(RegisterCapabilitiesEvent.class)) {
            bus.addListener(NeoForgeBlockItemEvents::onRegisterCapabilities);
        }
        NeoForge.EVENT_BUS.addListener(NeoForgeBlockItemEvents::onFurnaceFuelBurnTime);
        NeoForge.EVENT_BUS.addListener(NeoForgeBlockItemEvents::onToolModification);
    }

    private static void onToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getItemAbility() == ItemAbilities.SHOVEL_FLATTEN && event.getState().is(TheabyssModBlocks.LUSH_ROOTED_DIRT.get())) {
            event.setFinalState(Blocks.DIRT_PATH.defaultBlockState());
        }
    }

    private static void onFurnaceFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        int burnTime = AbyssFuels.burnTime(event.getItemStack());
        if (burnTime >= 0) {
            event.setBurnTime(burnTime);
        } else if (AbyssFuels.isModItem(event.getItemStack().getItem())) {
            event.setBurnTime(0);
        }
    }

    private static void onRegisterCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        AbyssEventBus.post(new RegisterCapabilitiesEvent() {
            @Override
            public <BE extends BlockEntity & WorldlyContainer> void registerSidedItemContainer(BlockEntityType<BE> type) {
                event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, SidedInvWrapper::new);
            }

            @Override
            public <BE extends BlockEntity & Container> void registerItemContainer(BlockEntityType<BE> type) {
                event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (entity, side) -> new InvWrapper(entity));
            }

            @Override
            public void registerChestItemContainer(Block... blocks) {
                event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, entity, side) -> {
                    Container container = ChestBlock.getContainer((ChestBlock)state.getBlock(), state, level, pos, true);
                    return container == null ? null : new InvWrapper(container);
                }, blocks);
            }
        });
    }
}
