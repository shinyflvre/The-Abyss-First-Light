package net.yezon.theabyss.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.yezon.theabyss.init.TheabyssModBlocks;

public class PhantomLanternBlock extends AbyssLanternBlock {
    public PhantomLanternBlock(BlockBehaviour.Properties properties) {
        super(properties, TheabyssModBlocks.PHANTOM_LANTERN, false);
    }
}
