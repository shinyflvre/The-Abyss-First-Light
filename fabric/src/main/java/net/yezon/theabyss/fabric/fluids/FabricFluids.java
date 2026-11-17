package net.yezon.theabyss.fabric.fluids;

import java.util.Optional;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.yezon.theabyss.init.TheabyssModFluids;
import net.yezon.theabyss.platform.common.SoundActions;
import net.yezon.theabyss.platform.fluids.BaseFlowingFluid;
import net.yezon.theabyss.platform.fluids.FluidType;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import org.jspecify.annotations.Nullable;

public final class FabricFluids {
    private FabricFluids() {
    }

    public static void install() {
        for (DeferredHolder<Fluid, ? extends Fluid> holder : TheabyssModFluids.REGISTRY.getEntries()) {
            if (holder.get() instanceof BaseFlowingFluid fluid) {
                FluidVariantAttributes.register(fluid, new Attributes(fluid.abyssTypeSupplier()));
            }
        }
    }

    private record Attributes(Supplier<? extends FluidType> type) implements FluidVariantAttributeHandler {
        @Override
        public Component getName(FluidVariant variant) {
            return Component.translatable(descriptionId(this.type));
        }

        @Override
        public Optional<SoundEvent> getFillSound(FluidVariant variant) {
            return Optional.ofNullable(this.type.get().getSound(SoundActions.BUCKET_FILL));
        }

        @Override
        public Optional<SoundEvent> getEmptySound(FluidVariant variant) {
            return Optional.ofNullable(this.type.get().getSound(SoundActions.BUCKET_EMPTY));
        }

        @Override
        public int getLuminance(FluidVariant variant) {
            return this.type.get().getLightLevel();
        }

        @Override
        public int getTemperature(FluidVariant variant) {
            return this.type.get().getTemperature();
        }

        @Override
        public int getViscosity(FluidVariant variant, @Nullable Level level) {
            return this.type.get().getViscosity();
        }

        @Override
        public boolean isLighterThanAir(FluidVariant variant) {
            return this.type.get().getDensity() <= 0;
        }
    }

    private static String descriptionId(Supplier<? extends FluidType> type) {
        String explicit = type.get().descriptionId();
        if (explicit != null) {
            return explicit;
        }
        if (type instanceof DeferredHolder<?, ?> holder) {
            return Util.makeDescriptionId("fluid_type", holder.getId());
        }
        return "fluid_type.unregistered";
    }
}
