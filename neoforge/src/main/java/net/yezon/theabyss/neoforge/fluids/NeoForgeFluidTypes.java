package net.yezon.theabyss.neoforge.fluids;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yezon.theabyss.platform.fluids.FluidType;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;

public final class NeoForgeFluidTypes {
    private static final Map<FluidType, NeoForgeAbyssFluidType> BY_COMMON = new ConcurrentHashMap<>();

    private NeoForgeFluidTypes() {
    }

    @SuppressWarnings("unchecked")
    public static void register(DeferredRegister<?> register, IEventBus bus) {
        DeferredRegister<FluidType> common = (DeferredRegister<FluidType>)register;
        net.neoforged.neoforge.registries.DeferredRegister<net.neoforged.neoforge.fluids.FluidType> neo =
            net.neoforged.neoforge.registries.DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, common.getNamespace());
        for (DeferredHolder<FluidType, ? extends FluidType> holder : common.getEntries()) {
            neo.register(holder.getId().getPath(), () -> wrap(holder.get()));
        }
        common.getAliases().forEach(neo::addAlias);
        common.markRegistered();
        neo.register(bus);
    }

    public static net.neoforged.neoforge.fluids.FluidType of(FluidType common) {
        NeoForgeAbyssFluidType type = BY_COMMON.get(common);
        if (type == null) {
            throw new IllegalStateException("Fluid type used before the fluid type registry was filled: " + common);
        }
        return type;
    }

    private static NeoForgeAbyssFluidType wrap(FluidType common) {
        return BY_COMMON.computeIfAbsent(common, NeoForgeAbyssFluidType::new);
    }
}
