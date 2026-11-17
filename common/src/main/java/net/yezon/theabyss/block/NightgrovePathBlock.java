package net.yezon.theabyss.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.yezon.theabyss.init.TheabyssModBlocks;

public class NightgrovePathBlock extends AbyssPathBlock {
    public NightgrovePathBlock(BlockBehaviour.Properties properties) {
        super(TheabyssModBlocks.NIGHTGROVE_DIRT.get(), properties.mapColor(MapColor.COLOR_BLUE).strength(0.65F).sound(SoundType.GRASS)
            .isSuffocating((state, level, pos) -> true));
    }
}
