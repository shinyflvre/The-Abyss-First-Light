package net.yezon.theabyss.fabric.fluids;

import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.world.level.material.Fluid;
import net.yezon.theabyss.platform.client.event.RegisterFluidModelsEvent;
import net.yezon.theabyss.platform.event.AbyssEventBus;

public final class FabricFluidsClient {
    private FabricFluidsClient() {
    }

    public static void install() {
        AbyssEventBus.post(new RegisterFluidModelsEvent() {
            @Override
            public void register(FluidModel.Unbaked model, Supplier<? extends Fluid> fluid) {
                FluidRenderingRegistry.register(fluid.get(), model);
            }

            @Override
            public void register(FluidModel.Unbaked model, Supplier<? extends Fluid> stillFluid, Supplier<? extends Fluid> flowingFluid) {
                FluidRenderingRegistry.register(stillFluid.get(), flowingFluid.get(), model);
            }
        });
    }
}
