package net.yezon.theabyss.block;

import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.yezon.theabyss.init.TheabyssModBlocks;

public class NightgroveFarmlandBlock extends FarmlandBlock {
    public NightgroveFarmlandBlock(BlockBehaviour.Properties properties) {
        super(TheabyssModBlocks.NIGHTGROVE_DIRT.get(), properties.mapColor(MapColor.COLOR_BLUE).randomTicks().strength(0.6F).sound(SoundType.GRAVEL)
            .isSuffocating((state, level, pos) -> true));
    }
}
