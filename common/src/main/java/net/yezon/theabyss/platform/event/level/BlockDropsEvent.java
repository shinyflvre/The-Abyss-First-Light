package net.yezon.theabyss.platform.event.level;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public class BlockDropsEvent extends Event implements ICancellableEvent {
    private final java.util.List<net.minecraft.world.entity.item.ItemEntity> drops;
    private final net.minecraft.server.level.ServerLevel level;
    private final net.minecraft.core.BlockPos pos;
    private final net.minecraft.world.level.block.state.BlockState state;
    private final net.minecraft.world.item.ItemStack tool;

    public BlockDropsEvent(java.util.List<net.minecraft.world.entity.item.ItemEntity> drops, net.minecraft.server.level.ServerLevel level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.item.ItemStack tool) {
        this.drops = drops;
        this.level = level;
        this.pos = pos;
        this.state = state;
        this.tool = tool;
    }

    public java.util.List<net.minecraft.world.entity.item.ItemEntity> getDrops() {
        return this.drops;
    }

    public net.minecraft.server.level.ServerLevel getLevel() {
        return this.level;
    }

    public net.minecraft.core.BlockPos getPos() {
        return this.pos;
    }

    public net.minecraft.world.level.block.state.BlockState getState() {
        return this.state;
    }

    public net.minecraft.world.item.ItemStack getTool() {
        return this.tool;
    }
}
