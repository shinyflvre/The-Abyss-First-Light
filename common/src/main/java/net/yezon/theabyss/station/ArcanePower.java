package net.yezon.theabyss.station;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.yezon.theabyss.block.entity.AbyssalEnergeticaBlockEntity;
import net.yezon.theabyss.init.TheabyssModBlocks;
import org.jspecify.annotations.Nullable;

public final class ArcanePower {
    public static final int[][] CORNERS = {{-2, -2}, {2, -2}, {2, 2}, {-2, 2}};

    private ArcanePower() {
    }

    public static boolean powered(BlockGetter level, BlockPos arcane) {
        for (int[] c : CORNERS) {
            BlockEntity be = level.getBlockEntity(arcane.offset(c[0], 0, c[1]));
            if (!(be instanceof AbyssalEnergeticaBlockEntity pillar) || !pillar.charged()) {
                return false;
            }
        }
        return true;
    }

    public static @Nullable BlockPos arcaneOf(BlockGetter level, BlockPos pillar) {
        for (int[] c : CORNERS) {
            BlockPos at = pillar.offset(-c[0], 0, -c[1]);
            if (level.getBlockState(at).is(TheabyssModBlocks.ARCANE_WORKBENCH.get())) {
                return at;
            }
        }
        return null;
    }
}
