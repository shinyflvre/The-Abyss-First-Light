package net.yezon.theabyss.fabric.mixin.client;

import net.minecraft.core.IdMapper;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockColors.class)
public interface BlockColorsAccessor {
    @Accessor("blockColors")
    IdMapper<BlockColor> theabyss$blockColors();
}
