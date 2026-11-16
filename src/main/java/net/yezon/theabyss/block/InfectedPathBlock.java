package net.yezon.theabyss.block;

import net.minecraft.world.level.block.PathBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.yezon.theabyss.init.TheabyssModBlocks;

public class InfectedPathBlock extends PathBlock {
    public InfectedPathBlock(BlockBehaviour.Properties properties) {
        super(TheabyssModBlocks.DIRT.get(), properties.mapColor(MapColor.COLOR_CYAN).strength(0.65F).sound(SoundType.GRASS)
            .isSuffocating((state, level, pos) -> true));
    }
}
