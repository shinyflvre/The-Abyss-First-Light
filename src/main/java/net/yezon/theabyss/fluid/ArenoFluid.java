package net.yezon.theabyss.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.yezon.theabyss.init.TheabyssModBlocks;
import net.yezon.theabyss.init.TheabyssModFluidTypes;
import net.yezon.theabyss.init.TheabyssModFluids;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import org.jspecify.annotations.Nullable;

public abstract class ArenoFluid extends BaseFlowingFluid {
    public static final BaseFlowingFluid.Properties PROPERTIES = new BaseFlowingFluid.Properties(
            TheabyssModFluidTypes.ARENO_TYPE, TheabyssModFluids.ARENO, TheabyssModFluids.FLOWING_ARENO
        )
        .explosionResistance(100.0F)
        .bucket(TheabyssModItems.ARENO_BUCKET)
        .block(() -> (LiquidBlock) TheabyssModBlocks.ARENO.get());

    private ArenoFluid() {
        super(PROPERTIES);
    }

    @Override
    protected void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
        FluidSprings.pop(level, pos, random, TheabyssModParticleTypes.ARENO_EMBER.get(), TheabyssModParticleTypes.ARENO_SPLASH.get());
    }

    @Override
    protected @Nullable ParticleOptions getDripParticle() {
        return TheabyssModParticleTypes.DRIPPING_ARENO.get();
    }

    public static class Flowing extends ArenoFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Source extends ArenoFluid {
        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
