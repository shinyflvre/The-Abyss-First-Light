package net.yezon.theabyss.init;

import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
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

        private static FluidModel.Unbaked translucentModel(String texture) {
            Material material = new Material(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, texture), true);
            return new FluidModel.Unbaked(material, material, null, (FluidTintSource) null);
        }
    }
}
