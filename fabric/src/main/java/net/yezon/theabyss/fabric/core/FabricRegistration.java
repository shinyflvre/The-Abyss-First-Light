package net.yezon.theabyss.fabric.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.yezon.theabyss.platform.attachment.AttachmentType;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.platform.registries.NeoForgeRegistries;

public final class FabricRegistration {
    private FabricRegistration() {
    }

    @SuppressWarnings("unchecked")
    public static void register(List<DeferredRegister<?>> registers, Consumer<DeferredRegister<?>> fluidTypes) {
        Map<ResourceKey<?>, List<DeferredRegister<?>>> byRegistry = new LinkedHashMap<>();
        for (DeferredRegister<?> register : registers) {
            if (register.getRegistryKey().equals(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)) {
                FabricAttachments.register((DeferredRegister<AttachmentType<?>>)register);
            } else if (register.getRegistryKey().equals(NeoForgeRegistries.Keys.FLUID_TYPES)) {
                fluidTypes.accept(register);
            } else {
                byRegistry.computeIfAbsent(register.getRegistryKey(), k -> new ArrayList<>()).add(register);
            }
        }
        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            List<DeferredRegister<?>> matching = byRegistry.remove(registry.key());
            if (matching != null) {
                for (DeferredRegister<?> register : matching) {
                    registerAll((Registry<Object>)registry, (DeferredRegister<Object>)register);
                }
            }
        }
        if (!byRegistry.isEmpty()) {
            throw new IllegalStateException("Unknown registries for The Abyss: " + byRegistry.keySet());
        }
    }

    private static void registerAll(Registry<Object> registry, DeferredRegister<Object> register) {
        register.getFactories().forEach((holder, factory) -> Registry.register(registry, holder.getKey(), factory.apply(holder.getId())));
        register.markRegistered();
    }
}
