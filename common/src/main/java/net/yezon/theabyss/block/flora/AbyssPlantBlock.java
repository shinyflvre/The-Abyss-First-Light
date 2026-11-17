package net.yezon.theabyss.block.flora;

import net.minecraft.world.phys.Vec3;
import com.mojang.serialization.MapCodec;
import net.yezon.theabyss.block.AbyssShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AbyssPlantBlock extends BushBlock {
    public static final MapCodec<AbyssPlantBlock> CODEC = simpleCodec(AbyssPlantBlock::new);
    private static final VoxelShape SHAPE = AbyssShapes.column(12.0, 0.0, 13.0);

    public AbyssPlantBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(level, pos);
        return SHAPE.move(offset.x, offset.y, offset.z);
    }

    @Override
    protected MapCodec<? extends AbyssPlantBlock> codec() {
        return CODEC;
    }
}
