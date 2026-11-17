package net.yezon.theabyss.platform.client.event;

import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.yezon.theabyss.platform.event.Event;
import org.jspecify.annotations.Nullable;

public abstract class RegisterFluidModelsEvent extends Event {
    public abstract void register(FluidModel model, Supplier<? extends Fluid> fluid);

    public abstract void register(FluidModel model, Supplier<? extends Fluid> stillFluid, Supplier<? extends Fluid> flowingFluid);

    public record FluidModel(ResourceLocation stillTexture, ResourceLocation flowingTexture, @Nullable ResourceLocation overlayTexture, int tint, boolean translucent) {
    }
}
