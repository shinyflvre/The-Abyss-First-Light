package net.yezon.theabyss.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.TriState;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public interface BlockItemHooks {
    static BlockItemHooks get() {
        BlockItemHooks hooks = Holder.instance;
        if (hooks == null) {
            throw new IllegalStateException("The Abyss block and item hooks were not installed by the loader");
        }
        return hooks;
    }

    static void install(BlockItemHooks hooks) {
        Holder.instance = hooks;
    }

    boolean canCropGrow(Level level, BlockPos pos, BlockState state, boolean def);

    void fireCropGrowPost(Level level, BlockPos pos, BlockState state);

    TriState canSustainPlant(BlockState soil, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant);

    CreativeModeTab.Builder creativeTabBuilder(boolean searchBar, TabContents contents);

    @FunctionalInterface
    interface TabContents {
        void accept(CreativeModeTab.ItemDisplayParameters parameters, TabOutput output);
    }

    interface TabOutput {
        void accept(ItemStack stack);

        void accept(ItemLike item);
    }

    final class Holder {
        private static volatile @Nullable BlockItemHooks instance;

        private Holder() {
        }
    }
}
