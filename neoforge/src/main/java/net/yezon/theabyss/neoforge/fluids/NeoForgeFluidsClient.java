package net.yezon.theabyss.neoforge.fluids;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.yezon.theabyss.platform.client.event.RegisterFluidModelsEvent;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import org.jspecify.annotations.Nullable;

public final class NeoForgeFluidsClient {
    private NeoForgeFluidsClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterClientExtensionsEvent.class, NeoForgeFluidsClient::onRegisterClientExtensions);
    }

    private static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        AbyssEventBus.post(new RegisterFluidModelsEvent() {
            @Override
            public void register(FluidModel model, Supplier<? extends Fluid> fluid) {
                apply(event, model, fluid.get());
            }

            @Override
            public void register(FluidModel model, Supplier<? extends Fluid> stillFluid, Supplier<? extends Fluid> flowingFluid) {
                apply(event, model, stillFluid.get(), flowingFluid.get());
            }
        });
    }

    private static void apply(RegisterClientExtensionsEvent event, RegisterFluidModelsEvent.FluidModel model, Fluid... fluids) {
        Set<FluidType> types = new LinkedHashSet<>();
        for (Fluid fluid : fluids) {
            types.add(fluid.getFluidType());
            if (model.translucent()) {
                ItemBlockRenderTypes.setRenderLayer(fluid, RenderType.translucent());
            }
        }
        event.registerFluidType(new Extensions(model), types.toArray(FluidType[]::new));
    }

    private record Extensions(RegisterFluidModelsEvent.FluidModel model) implements IClientFluidTypeExtensions {
        @Override
        public ResourceLocation getStillTexture() {
            return this.model.stillTexture();
        }

        @Override
        public ResourceLocation getFlowingTexture() {
            return this.model.flowingTexture();
        }

        @Override
        public @Nullable ResourceLocation getOverlayTexture() {
            return this.model.overlayTexture();
        }

        @Override
        public int getTintColor() {
            return this.model.tint();
        }
    }
}
