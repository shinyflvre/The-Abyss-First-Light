package net.yezon.theabyss.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.yezon.theabyss.block.hooks.FireSpreadingBlock;
import net.yezon.theabyss.block.hooks.PlayerDestroyListener;
import net.yezon.theabyss.events.BlaruLeavesHandlerEvents;

public class BlaruLeavesBlock extends Block implements FireSpreadingBlock, PlayerDestroyListener {
    public BlaruLeavesBlock(BlockBehaviour.Properties properties) {
        super(properties.ignitedByLava().sound(SoundType.CHERRY_LEAVES).strength(0.3F, 5.0F).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 1;
    }

    @Override
    public void afterDestroyedByPlayer(BlockState blockstate, Level world, BlockPos pos, Player entity, ItemStack toolStack, boolean willHarvest, FluidState fluid) {
        BlaruLeavesHandlerEvents.execute(world, pos.getX(), pos.getY(), pos.getZ(), entity);
    }
}
