package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterColorHandlersEvent extends Event {

    public static abstract class BlockTintSources extends RegisterColorHandlersEvent {
        public abstract net.minecraft.client.color.block.BlockColors getBlockColors();
        public abstract void register(java.util.List<net.minecraft.client.color.block.BlockTintSource> a0, net.minecraft.world.level.block.Block... a1);
    }
}
