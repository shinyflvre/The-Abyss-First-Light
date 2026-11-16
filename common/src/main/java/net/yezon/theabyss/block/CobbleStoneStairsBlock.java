package net.yezon.theabyss.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.storage.loot.LootParams;

public class CobbleStoneStairsBlock extends StairBlock {
    public CobbleStoneStairsBlock(BlockBehaviour.Properties properties) {
        super(
            Blocks.AIR.defaultBlockState(),
            properties.instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.STONE).strength(3.0F, 10.0F).requiresCorrectToolForDrops().dynamicShape()
        );
    }

    @Override
    public float getExplosionResistance() {
        return 10.0F;
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return 0;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(this, 1));
    }
}
