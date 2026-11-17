package net.yezon.theabyss.neoforge.core;

import java.util.List;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.yezon.theabyss.platform.attachment.AttachmentType;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.platform.registries.NeoForgeRegistries;

public final class NeoForgeRegistration {
    private NeoForgeRegistration() {
    }

    @SuppressWarnings("unchecked")
    public static void register(List<DeferredRegister<?>> registers, IEventBus bus, PlatformManagedRegistry fluidTypes) {
        for (DeferredRegister<?> register : registers) {
            if (register.getRegistryKey().equals(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)) {
                NeoForgeAttachments.register((DeferredRegister<AttachmentType<?>>)register, bus);
            } else if (register.getRegistryKey().equals(NeoForgeRegistries.Keys.FLUID_TYPES)) {
                fluidTypes.register(register, bus);
            } else {
                registerVanilla(register, bus);
            }
        }
    }

    private static <T> void registerVanilla(DeferredRegister<T> common, IEventBus bus) {
        net.neoforged.neoforge.registries.DeferredRegister<T> neo =
            net.neoforged.neoforge.registries.DeferredRegister.create(common.getRegistryKey(), common.getNamespace());
        common.getFactories().forEach((DeferredHolder<T, ? extends T> holder, Function<ResourceLocation, ? extends T> factory) ->
            neo.register(holder.getId().getPath(), factory));
        common.getAliases().forEach(neo::addAlias);
        common.markRegistered();
        neo.register(bus);
    }

    @FunctionalInterface
    public interface PlatformManagedRegistry {
        void register(DeferredRegister<?> register, IEventBus bus);
    }
}
