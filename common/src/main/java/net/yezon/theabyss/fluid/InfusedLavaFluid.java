package net.yezon.theabyss.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.yezon.theabyss.platform.fluids.BaseFlowingFluid;
import net.yezon.theabyss.init.TheabyssModBlocks;
import net.yezon.theabyss.init.TheabyssModFluidTypes;
import net.yezon.theabyss.init.TheabyssModFluids;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import org.jspecify.annotations.Nullable;

public abstract class InfusedLavaFluid extends BaseFlowingFluid {
    public static final BaseFlowingFluid.Properties PROPERTIES = new BaseFlowingFluid.Properties(
            TheabyssModFluidTypes.INFUSED_LAVA_TYPE, TheabyssModFluids.INFUSED_LAVA, TheabyssModFluids.FLOWING_INFUSED_LAVA
        )
        .explosionResistance(100.0F)
        .bucket(TheabyssModItems.INFUSED_LAVA_BUCKET)
        .block(() -> (LiquidBlock) TheabyssModBlocks.INFUSED_LAVA.get());

    private InfusedLavaFluid() {
        super(PROPERTIES);
    }

    @Override
    protected void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
        FluidSprings.pop(level, pos, random, TheabyssModParticleTypes.INFUSED_LAVA_EMBER.get(), TheabyssModParticleTypes.INFUSED_LAVA_SPLASH.get());
    }

    @Override
    protected @Nullable ParticleOptions getDripParticle() {
        return TheabyssModParticleTypes.DRIPPING_INFUSED_LAVA.get();
    }

    public static class Flowing extends InfusedLavaFluid {
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

    public static class Source extends InfusedLavaFluid {
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
