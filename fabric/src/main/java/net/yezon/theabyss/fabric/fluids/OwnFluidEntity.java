package net.yezon.theabyss.fabric.fluids;

import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.jspecify.annotations.Nullable;

public interface OwnFluidEntity {
    Object2DoubleArrayMap<FluidType> theabyss$fluidHeights();

    @Nullable FluidType theabyss$eyeFluid();

    void theabyss$setEyeFluid(@Nullable FluidType type);
}
