package net.yezon.theabyss.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.yezon.theabyss.events.ArenoDamageHandlerEvents;
import net.yezon.theabyss.events.ArenoParticlesEvents;
import net.yezon.theabyss.init.TheabyssModFluids;

public class ArenoBlock extends LiquidBlock {
    public ArenoBlock(BlockBehaviour.Properties properties) {
        super(
            TheabyssModFluids.ARENO.get(),
            properties
                .mapColor(MapColor.WATER)
                .strength(100.0F)
                .postProcess((bs, br, bp) -> bp)
                .emissiveRendering(bs -> true)
                .lightLevel(s -> 8)
                .noCollision()
                .noLootTable()
                .liquid()
                .pushReaction(PushReaction.POPPED)
                .sound(SoundType.EMPTY)
                .replaceable()
        );
    }

    @Override
    protected void onPlace(BlockState blockstate, Level world, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(blockstate, world, pos, oldState, moving);
        world.scheduleTick(pos, this, 10);
    }

    @Override
    protected void tick(BlockState blockstate, ServerLevel world, BlockPos pos, RandomSource random) {
        super.tick(blockstate, world, pos, random);
        ArenoParticlesEvents.execute(world, pos.getX(), pos.getY(), pos.getZ());
        world.scheduleTick(pos, this, 10);
    }

    @Override
    protected void entityInside(BlockState blockstate, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        super.entityInside(blockstate, world, pos, entity, effectApplier, isPrecise);
        ArenoDamageHandlerEvents.execute(world, entity);
    }
}
