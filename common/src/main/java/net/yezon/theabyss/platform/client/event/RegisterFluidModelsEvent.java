package net.yezon.theabyss.platform.client.event;

import java.util.function.Supplier;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.world.level.material.Fluid;
import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterFluidModelsEvent extends Event {
    public abstract void register(FluidModel.Unbaked model, Supplier<? extends Fluid> fluid);

    public abstract void register(FluidModel.Unbaked model, Supplier<? extends Fluid> stillFluid, Supplier<? extends Fluid> flowingFluid);
}
