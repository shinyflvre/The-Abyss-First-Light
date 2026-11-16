package net.yezon.theabyss.fabric.mixin.fluids;

import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.entity.EntityFluidInteraction$Tracker")
public interface FluidTrackerAccessor {
    @Accessor("fluidType")
    Holder<Fluid> theabyss$fluidType();

    @Accessor("height")
    double theabyss$height();

    @Accessor("eyesInside")
    boolean theabyss$eyesInside();
}
