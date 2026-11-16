package net.yezon.theabyss.neoforge.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.TriState;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.yezon.theabyss.platform.services.BlockItemHooks;

public final class NeoForgeBlockItemHooks implements BlockItemHooks {
    @Override
    public boolean canCropGrow(Level level, BlockPos pos, BlockState state, boolean def) {
        return CommonHooks.canCropGrow(level, pos, state, def);
    }

    @Override
    public void fireCropGrowPost(Level level, BlockPos pos, BlockState state) {
        CommonHooks.fireCropGrowPost(level, pos, state);
    }

    @Override
    public TriState canSustainPlant(BlockState soil, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
        return soil.canSustainPlant(level, soilPosition, facing, plant);
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder(boolean searchBar, TabContents contents) {
        CreativeModeTab.Builder builder = CreativeModeTab.builder();
        if (searchBar) {
            builder.withSearchBar();
        }
        return builder.displayItems((parameters, output) -> contents.accept(parameters, wrap(output)));
    }

    private static TabOutput wrap(CreativeModeTab.Output output) {
        return new TabOutput() {
            @Override
            public void accept(ItemStack stack) {
                output.accept(stack);
            }

            @Override
            public void accept(ItemLike item) {
                output.accept(item);
            }
        };
    }
}
