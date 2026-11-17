package net.yezon.theabyss.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AbyssShapes {
    private AbyssShapes() {
    }

    public static VoxelShape column(double sizeXZ, double minY, double maxY) {
        return column(sizeXZ, sizeXZ, minY, maxY);
    }

    public static VoxelShape column(double sizeX, double sizeZ, double minY, double maxY) {
        double halfX = sizeX / 2.0;
        double halfZ = sizeZ / 2.0;
        return Block.box(8.0 - halfX, minY, 8.0 - halfZ, 8.0 + halfX, maxY, 8.0 + halfZ);
    }
}
