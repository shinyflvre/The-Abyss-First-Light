package net.yezon.theabyss.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.RegisterFluidModelsEvent;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.fluid.ArenoFluid;
import net.yezon.theabyss.fluid.InfusedLavaFluid;

public class TheabyssModFluids {
    public static final DeferredRegister<Fluid> REGISTRY = DeferredRegister.create(Registries.FLUID, TheabyssMod.MODID);
    public static final DeferredHolder<Fluid, FlowingFluid> ARENO = REGISTRY.register("areno", ArenoFluid.Source::new);
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_ARENO = REGISTRY.register("flowing_areno", ArenoFluid.Flowing::new);
    public static final DeferredHolder<Fluid, FlowingFluid> INFUSED_LAVA = REGISTRY.register("infused_lava", InfusedLavaFluid.Source::new);
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_INFUSED_LAVA = REGISTRY.register("flowing_infused_lava", InfusedLavaFluid.Flowing::new);

    @EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
    public static class ClientSideHandler {
        @SubscribeEvent
        public static void registerFluidModels(RegisterFluidModelsEvent event) {
            event.register(translucentModel("block/areno_still"), ARENO, FLOWING_ARENO);
            event.register(translucentModel("block/infused_magma"), INFUSED_LAVA, FLOWING_INFUSED_LAVA);
        }

        private static RegisterFluidModelsEvent.FluidModel translucentModel(String texture) {
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, texture);
            return new RegisterFluidModelsEvent.FluidModel(location, location, null, 0xFFFFFFFF, true);
        }
    }
}
