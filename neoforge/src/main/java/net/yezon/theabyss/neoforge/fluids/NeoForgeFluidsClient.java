package net.yezon.theabyss.neoforge.fluids;

import java.util.function.Supplier;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.yezon.theabyss.platform.client.event.RegisterFluidModelsEvent;
import net.yezon.theabyss.platform.event.AbyssEventBus;

public final class NeoForgeFluidsClient {
    private NeoForgeFluidsClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(net.neoforged.neoforge.client.event.RegisterFluidModelsEvent.class, event -> AbyssEventBus.post(new RegisterFluidModelsEvent() {
            @Override
            public void register(FluidModel.Unbaked model, Supplier<? extends Fluid> fluid) {
                event.register(model, fluid);
            }

            @Override
            public void register(FluidModel.Unbaked model, Supplier<? extends Fluid> stillFluid, Supplier<? extends Fluid> flowingFluid) {
                event.register(model, stillFluid, flowingFluid);
            }
        }));
    }
}
