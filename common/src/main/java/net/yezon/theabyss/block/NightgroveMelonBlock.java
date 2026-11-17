package net.yezon.theabyss.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yezon.theabyss.init.TheabyssModItems;

public class NightgroveMelonBlock extends Block {
    public NightgroveMelonBlock(BlockBehaviour.Properties properties) {
        super(
            properties
                .sound(SoundType.CROP)
                .strength(0.6F, 10.0F)
                .lightLevel(s -> 5)
                .hasPostProcess((bs, br, bp) -> true)
        );
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(TheabyssModItems.NIGHTGROVE_MELON_ITEM.get(), 3));
    }
}
