package net.yezon.theabyss.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.yezon.theabyss.events.GlowingBlaruMossParticleEvents;
import net.yezon.theabyss.init.TheabyssModBlocks;

public class GlowingBlaruMossBlock extends FlowerBlock {
    public GlowingBlaruMossBlock(BlockBehaviour.Properties properties) {
        super(
            MobEffects.SPEED,
            5.0F,
            properties
                .mapColor(MapColor.PLANT)
                .sound(SoundType.NYLIUM)
                .instabreak()
                .postProcess((bs, br, bp) -> bp)
                .emissiveRendering(bs -> true)
                .lightLevel(s -> 6)
                .noCollision()
                .replaceable()
                .offsetType(OffsetType.NONE)
                .pushReaction(PushReaction.POPPED)
        );
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
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
    protected boolean mayPlaceOn(BlockState groundState, BlockGetter worldIn, BlockPos pos) {
        return groundState.is(TheabyssModBlocks.BLARU_LOG.get())
            || groundState.is(TheabyssModBlocks.LUNA_LOG.get())
            || groundState.is(TheabyssModBlocks.RENA_LOG.get());
    }

    @Override
    protected boolean canSurvive(BlockState blockstate, LevelReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.below();
        BlockState groundState = worldIn.getBlockState(blockpos);
        return this.mayPlaceOn(groundState, worldIn, blockpos);
    }

    @Override
    protected void entityInside(BlockState blockstate, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        super.entityInside(blockstate, world, pos, entity, effectApplier, isPrecise);
        GlowingBlaruMossParticleEvents.execute(world, pos.getY(), entity);
    }

    @Override
    public void stepOn(Level world, BlockPos pos, BlockState blockstate, Entity entity) {
        super.stepOn(world, pos, blockstate, entity);
        GlowingBlaruMossParticleEvents.execute(world, pos.getY(), entity);
    }
}
