package net.yezon.theabyss.platform.registries;

import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterEvent extends Event {
    public abstract <T> void register(net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<T>> a0, net.minecraft.resources.Identifier a1, java.util.function.Supplier<T> a2);
}
