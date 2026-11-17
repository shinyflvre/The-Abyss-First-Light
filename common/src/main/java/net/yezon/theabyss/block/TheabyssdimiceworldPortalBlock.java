package net.yezon.theabyss.block;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.portal.DimensionTransition;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.world.teleporter.TheabyssdimiceworldPortalShape;
import net.yezon.theabyss.world.teleporter.TheabyssdimiceworldTeleporter;
import org.jspecify.annotations.Nullable;

public class TheabyssdimiceworldPortalBlock extends NetherPortalBlock {
    public static final ResourceKey<Level> DESTINATION = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("theabyss", "frost_world"));

    public TheabyssdimiceworldPortalBlock(BlockBehaviour.Properties properties) {
        super(
            properties.noCollission()
                .randomTicks()
                .pushReaction(PushReaction.BLOCK)
                .strength(-1.0F)
                .sound(SoundType.GLASS)
                .lightLevel(s -> 0)
                .noLootTable()
        );
    }

    public static void portalSpawn(Level world, BlockPos pos) {
        TheabyssdimiceworldPortalShape.findEmptyPortalShape(world, pos, Axis.X).ifPresent(shape -> shape.createPortalBlocks(world));
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction directionToNeighbour, BlockState neighbourState, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        Axis updateAxis = directionToNeighbour.getAxis();
        Axis axis = state.getValue(AXIS);
        boolean wrongAxis = axis != updateAxis && updateAxis.isHorizontal();
        return !wrongAxis && !neighbourState.is(this) && !new TheabyssdimiceworldPortalShape(level, pos, axis).isComplete()
            ? Blocks.AIR.defaultBlockState()
            : state;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 4; i++) {
            double px = pos.getX() + random.nextFloat();
            double py = pos.getY() + random.nextFloat();
            double pz = pos.getZ() + random.nextFloat();
            double vx = (random.nextFloat() - 0.5) / 2.0;
            double vy = (random.nextFloat() - 0.5) / 2.0;
            double vz = (random.nextFloat() - 0.5) / 2.0;
            int j = random.nextInt(4) - 1;
            if (!world.getBlockState(pos.west()).is(this) && !world.getBlockState(pos.east()).is(this)) {
                px = pos.getX() + 0.5 + 0.25 * j;
                vx = random.nextFloat() * 2.0F * j;
            } else {
                pz = pos.getZ() + 0.5 + 0.25 * j;
                vz = random.nextFloat() * 2.0F * j;
            }

            world.addParticle(TheabyssModParticleTypes.SPURS_FX.get(), px, py, pz, vx, vy, vz);
        }
    }

    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        return 0;
    }

    @Override
    public Portal.Transition getLocalTransition() {
        return Portal.Transition.NONE;
    }

    @Override
    public @Nullable DimensionTransition getPortalDestination(ServerLevel currentLevel, Entity entity, BlockPos portalEntryPos) {
        ResourceKey<Level> destinationType = currentLevel.dimension() != DESTINATION ? DESTINATION : Level.OVERWORLD;
        ServerLevel destination = currentLevel.getServer().getLevel(destinationType);
        return destination == null ? null : new TheabyssdimiceworldTeleporter(destination, portalEntryPos).getPortalDestination(entity);
    }
}
