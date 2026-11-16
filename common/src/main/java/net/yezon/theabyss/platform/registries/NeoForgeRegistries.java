package net.yezon.theabyss.platform.registries;

import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.yezon.theabyss.platform.attachment.AttachmentType;
import net.yezon.theabyss.platform.fluids.FluidType;

public final class NeoForgeRegistries {
    private NeoForgeRegistries() {
    }

    public static final class Keys {
        public static final ResourceKey<Registry<AttachmentType<?>>> ATTACHMENT_TYPES = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("neoforge", "attachment_types"));
        public static final ResourceKey<Registry<FluidType>> FLUID_TYPES = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("neoforge", "fluid_type"));

        static final Set<ResourceKey<?>> PLATFORM_MANAGED = Set.of(ATTACHMENT_TYPES, FLUID_TYPES);

        private Keys() {
        }
    }
}
