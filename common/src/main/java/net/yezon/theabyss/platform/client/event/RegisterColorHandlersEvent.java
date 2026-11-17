package net.yezon.theabyss.platform.client.event;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.level.ItemLike;
import net.yezon.theabyss.platform.event.Event;
import org.jspecify.annotations.Nullable;

public abstract class RegisterColorHandlersEvent extends Event {

    public static abstract class Block extends RegisterColorHandlersEvent {
        public abstract BlockColors getBlockColors();

        public abstract void register(BlockColor color, net.minecraft.world.level.block.Block... blocks);

        public abstract @Nullable BlockColor getColor(net.minecraft.world.level.block.Block block);
    }

    public static abstract class Item extends RegisterColorHandlersEvent {
        public abstract ItemColors getItemColors();

        public abstract BlockColors getBlockColors();

        public abstract void register(ItemColor color, ItemLike... items);
    }
}
