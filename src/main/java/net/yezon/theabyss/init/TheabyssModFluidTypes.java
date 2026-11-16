package net.yezon.theabyss.init;

import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.fluid.types.ArenoFluidType;
import net.yezon.theabyss.fluid.types.InfusedLavaFluidType;

public class TheabyssModFluidTypes {
    public static final DeferredRegister<FluidType> REGISTRY = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, TheabyssMod.MODID);
    public static final DeferredHolder<FluidType, FluidType> ARENO_TYPE = REGISTRY.register("areno", ArenoFluidType::new);
    public static final DeferredHolder<FluidType, FluidType> INFUSED_LAVA_TYPE = REGISTRY.register("infused_lava", InfusedLavaFluidType::new);
}
