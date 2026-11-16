package net.yezon.theabyss.fabric.blocks;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.TriState;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.platform.services.BlockItemHooks;

public final class FabricBlockItemHooks implements BlockItemHooks {
    @Override
    public boolean canCropGrow(Level level, BlockPos pos, BlockState state, boolean def) {
        return def;
    }

    @Override
    public void fireCropGrowPost(Level level, BlockPos pos, BlockState state) {
    }

    @Override
    public TriState canSustainPlant(BlockState soil, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
        return FabricSoil.decision(soil, level, soilPosition, facing, plant);
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder(boolean searchBar, TabContents contents) {
        return FabricCreativeModeTab.builder().displayItems((parameters, output) -> contents.accept(parameters, wrap(output)));
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
