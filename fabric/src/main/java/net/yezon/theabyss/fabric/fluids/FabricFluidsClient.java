package net.yezon.theabyss.fabric.fluids;

import java.util.function.Supplier;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.material.Fluid;
import net.yezon.theabyss.platform.client.event.RegisterFluidModelsEvent;
import net.yezon.theabyss.platform.event.AbyssEventBus;

public final class FabricFluidsClient {
    private FabricFluidsClient() {
    }

    public static void install() {
        AbyssEventBus.post(new RegisterFluidModelsEvent() {
            @Override
            public void register(FluidModel model, Supplier<? extends Fluid> fluid) {
                apply(model, fluid.get());
            }

            @Override
            public void register(FluidModel model, Supplier<? extends Fluid> stillFluid, Supplier<? extends Fluid> flowingFluid) {
                apply(model, stillFluid.get(), flowingFluid.get());
            }
        });
    }

    private static void apply(RegisterFluidModelsEvent.FluidModel model, Fluid... fluids) {
        SimpleFluidRenderHandler handler = new SimpleFluidRenderHandler(model.stillTexture(), model.flowingTexture(), model.overlayTexture(), model.tint());
        for (Fluid fluid : fluids) {
            FluidRenderHandlerRegistry.INSTANCE.register(fluid, handler);
        }
        if (model.translucent()) {
            BlockRenderLayerMap.INSTANCE.putFluids(RenderType.translucent(), fluids);
        }
    }
}
