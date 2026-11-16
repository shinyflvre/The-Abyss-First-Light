package net.yezon.theabyss.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.yezon.theabyss.init.TheabyssModBlocks;

public class HoglinLanternBlock extends AbyssLanternBlock {
    public HoglinLanternBlock(BlockBehaviour.Properties properties) {
        super(properties, TheabyssModBlocks.HOGLIN_LANTERN, false);
    }
}
