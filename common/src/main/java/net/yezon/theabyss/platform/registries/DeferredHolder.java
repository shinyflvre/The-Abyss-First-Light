package net.yezon.theabyss.platform.registries;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.Nullable;

public class DeferredHolder<R, T extends R> implements Supplier<T> {
    protected final ResourceKey<R> key;
    private volatile Holder.@Nullable Reference<R> holder;

    protected DeferredHolder(ResourceKey<R> key) {
        this.key = key;
    }

    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<? extends Registry<R>> registryKey, Identifier valueName) {
        return new DeferredHolder<>(ResourceKey.create(registryKey, valueName));
    }

    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<R> key) {
        return new DeferredHolder<>(key);
    }

    @SuppressWarnings("unchecked")
    private Holder.@Nullable Reference<R> bind(boolean required) {
        Holder.Reference<R> bound = this.holder;
        if (bound != null) {
            return bound;
        }
        Registry<R> registry = (Registry<R>)BuiltInRegistries.REGISTRY.getValue(this.key.registry());
        if (registry != null) {
            bound = registry.get(this.key).orElse(null);
        }
        if (bound == null) {
            if (required) {
                throw new IllegalStateException("Registry object not present: " + this.key);
            }
            return null;
        }
        this.holder = bound;
        return bound;
    }

    @SuppressWarnings("unchecked")
    public T value() {
        return (T)this.bind(true).value();
    }

    @Override
    public T get() {
        return this.value();
    }

    public Optional<T> asOptional() {
        return this.isBound() ? Optional.of(this.value()) : Optional.empty();
    }

    public Identifier getId() {
        return this.key.identifier();
    }

    public ResourceKey<R> getKey() {
        return this.key;
    }

    public Optional<ResourceKey<R>> unwrapKey() {
        return Optional.of(this.key);
    }

    public String getRegisteredName() {
        return this.key.identifier().toString();
    }

    public boolean isBound() {
        Holder.Reference<R> bound = this.bind(false);
        return bound != null && bound.isBound();
    }

    public Holder<R> getDelegate() {
        return this.bind(true);
    }

    public boolean is(TagKey<R> tag) {
        return this.getDelegate().is(tag);
    }

    public boolean is(Identifier id) {
        return this.key.identifier().equals(id);
    }

    public boolean is(ResourceKey<R> key) {
        return this.key.equals(key);
    }

    public boolean is(Holder<R> other) {
        return other.is(this.key);
    }

    public Stream<TagKey<R>> tags() {
        return this.getDelegate().tags();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof DeferredHolder<?, ?> other && this.key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public String toString() {
        return "DeferredHolder{" + this.key + "}";
    }
}
