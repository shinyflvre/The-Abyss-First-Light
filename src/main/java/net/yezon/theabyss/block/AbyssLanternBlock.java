package net.yezon.theabyss.block;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;

public class AbyssLanternBlock extends LanternBlock {
    private final Supplier<Block> dropAs;

    public AbyssLanternBlock(BlockBehaviour.Properties properties, Supplier<Block> dropAs, boolean hangingByDefault) {
        super(properties.mapColor(MapColor.METAL).forceSolidOn().strength(3.5F).sound(SoundType.LANTERN).lightLevel(s -> 15).noOcclusion()
            .pushReaction(PushReaction.POPPED));
        this.dropAs = dropAs;
        if (hangingByDefault) {
            this.registerDefaultState(this.defaultBlockState().setValue(HANGING, true));
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        return !drops.isEmpty() ? drops : Collections.singletonList(new ItemStack(this.dropAs.get()));
    }
}
