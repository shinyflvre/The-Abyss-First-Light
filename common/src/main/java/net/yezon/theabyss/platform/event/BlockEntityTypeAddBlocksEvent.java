package net.yezon.theabyss.platform.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class BlockEntityTypeAddBlocksEvent extends Event {
    public abstract void modify(net.minecraft.world.level.block.entity.BlockEntityType<?> a0, net.minecraft.world.level.block.Block... a1);
}
