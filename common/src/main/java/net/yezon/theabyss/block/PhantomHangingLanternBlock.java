package net.yezon.theabyss.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.yezon.theabyss.init.TheabyssModBlocks;

public class PhantomHangingLanternBlock extends AbyssLanternBlock {
    public PhantomHangingLanternBlock(BlockBehaviour.Properties properties) {
        super(properties, TheabyssModBlocks.PHANTOM_LANTERN, true);
    }
}
