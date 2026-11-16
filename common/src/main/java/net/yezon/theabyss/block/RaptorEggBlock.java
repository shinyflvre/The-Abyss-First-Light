package net.yezon.theabyss.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.yezon.theabyss.block.hooks.FireSpreadingBlock;
import net.yezon.theabyss.block.hooks.FlammableBlock;
import net.yezon.theabyss.block.hooks.PlayerDestroyListener;
import net.yezon.theabyss.events.RaptorEggEventEvents;

public class RaptorEggBlock extends FlowerBlock implements FlammableBlock, FireSpreadingBlock, PlayerDestroyListener {
    public RaptorEggBlock(BlockBehaviour.Properties properties) {
        super(
            MobEffects.SATURATION,
            0.0F,
            properties
                .mapColor(MapColor.PLANT)
                .sound(SoundType.STONE)
                .instabreak()
                .noCollision()
                .offsetType(OffsetType.XZ)
                .pushReaction(PushReaction.POPPED)
        );
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(pos);
        return box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0).move(offset.x, offset.y, offset.z);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 100;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 60;
    }

    @Override
    protected void entityInside(BlockState blockstate, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        super.entityInside(blockstate, world, pos, entity, effectApplier, isPrecise);
        RaptorEggEventEvents.execute(world, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void afterDestroyedByPlayer(BlockState blockstate, Level world, BlockPos pos, Player entity, ItemStack toolStack, boolean willHarvest, FluidState fluid) {
        RaptorEggEventEvents.execute(world, pos.getX(), pos.getY(), pos.getZ());
    }
}
