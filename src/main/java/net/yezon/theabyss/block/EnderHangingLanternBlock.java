package net.yezon.theabyss.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.yezon.theabyss.init.TheabyssModBlocks;

public class EnderHangingLanternBlock extends AbyssLanternBlock {
    public EnderHangingLanternBlock(BlockBehaviour.Properties properties) {
        super(properties, TheabyssModBlocks.ENDER_LANTERN, true);
    }
}
