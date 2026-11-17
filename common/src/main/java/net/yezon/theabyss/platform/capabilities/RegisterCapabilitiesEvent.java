package net.yezon.theabyss.platform.capabilities;

import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterCapabilitiesEvent extends Event {
    public abstract <BE extends BlockEntity & WorldlyContainer> void registerSidedItemContainer(BlockEntityType<BE> type);

    public abstract <BE extends BlockEntity & Container> void registerItemContainer(BlockEntityType<BE> type);

    public abstract void registerChestItemContainer(Block... blocks);
}
