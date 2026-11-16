package net.yezon.theabyss.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.portal.TeleportTransition;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.init.TheabyssModSounds;
import net.yezon.theabyss.world.teleporter.TheAbyssDimPortalShape;
import net.yezon.theabyss.world.teleporter.TheAbyssDimTeleporter;
import org.jspecify.annotations.Nullable;

public class TheAbyssDimPortalBlock extends NetherPortalBlock {
    public static final ResourceKey<Level> DESTINATION = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("theabyss", "the_abyss"));

    public TheAbyssDimPortalBlock(BlockBehaviour.Properties properties) {
        super(
            properties.noCollision()
                .randomTicks()
                .pushReaction(PushReaction.IMMOVEABLE)
                .strength(-1.0F)
                .sound(SoundType.GLASS)
                .lightLevel(s -> 7)
                .noLootTable()
        );
    }

    public static void portalSpawn(Level world, BlockPos pos) {
        TheAbyssDimPortalShape.findEmptyPortalShape(world, pos, Axis.X).ifPresent(shape -> shape.createPortalBlocks(world));
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
    }

    @Override
    protected BlockState updateShape(
        BlockState state,
        LevelReader level,
        ScheduledTickAccess ticks,
        BlockPos pos,
        Direction directionToNeighbour,
        BlockPos neighbourPos,
        BlockState neighbourState,
        RandomSource random
    ) {
        Axis updateAxis = directionToNeighbour.getAxis();
        Axis axis = state.getValue(AXIS);
        boolean wrongAxis = axis != updateAxis && updateAxis.isHorizontal();
        return !wrongAxis && !neighbourState.is(this) && !new TheAbyssDimPortalShape(level, pos, axis).isComplete()
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

            world.addParticle(TheabyssModParticleTypes.END_SWORD_PT.get(), px, py, pz, vx, vy, vz);
        }

        if (random.nextInt(110) == 0) {
            world.playLocalSound(
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                TheabyssModSounds.AMBIENTMUSIC.get(),
                SoundSource.BLOCKS,
                0.5F,
                random.nextFloat() * 0.4F + 0.8F,
                false
            );
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
    public @Nullable TeleportTransition getPortalDestination(ServerLevel currentLevel, Entity entity, BlockPos portalEntryPos) {
        ResourceKey<Level> destinationType = currentLevel.dimension() != DESTINATION ? DESTINATION : Level.OVERWORLD;
        ServerLevel destination = currentLevel.getServer().getLevel(destinationType);
        return destination == null ? null : new TheAbyssDimTeleporter(destination, portalEntryPos).getPortalDestination(entity);
    }
}
