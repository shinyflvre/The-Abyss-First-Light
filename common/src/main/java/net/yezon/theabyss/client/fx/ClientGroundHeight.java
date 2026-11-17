package net.yezon.theabyss.client.fx;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public final class ClientGroundHeight {
    private static final int WORLD_LIMIT = 30000000;

    private ClientGroundHeight() {
    }

    public static int noLeaves(Level level, int x, int z) {
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        if (x < -WORLD_LIMIT || z < -WORLD_LIMIT || x >= WORLD_LIMIT || z >= WORLD_LIMIT
            || !level.hasChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z))) {
            return top;
        }
        int bottom = level.getMinBuildHeight();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, top, z);
        for (int y = top - 1; y >= bottom; y--) {
            BlockState state = level.getBlockState(pos.setY(y));
            if ((state.blocksMotion() || !state.getFluidState().isEmpty()) && !state.is(BlockTags.LEAVES)) {
                return y + 1;
            }
        }
        return bottom;
    }
}
