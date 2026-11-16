package net.yezon.theabyss.platform.event.level.block;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public class BreakBlockEvent extends Event implements ICancellableEvent {
    private final net.minecraft.world.level.LevelAccessor level;
    private final net.minecraft.world.entity.player.Player player;
    private final net.minecraft.core.BlockPos pos;

    public BreakBlockEvent(net.minecraft.world.level.LevelAccessor level, net.minecraft.world.entity.player.Player player, net.minecraft.core.BlockPos pos) {
        this.level = level;
        this.player = player;
        this.pos = pos;
    }

    public net.minecraft.world.level.LevelAccessor getLevel() {
        return this.level;
    }

    public net.minecraft.world.entity.player.Player getPlayer() {
        return this.player;
    }

    public net.minecraft.core.BlockPos getPos() {
        return this.pos;
    }
}
