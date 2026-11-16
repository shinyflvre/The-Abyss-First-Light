package net.yezon.theabyss.platform.registries;

import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public final class ValueHolder<R, T extends R> extends DeferredHolder<R, T> {
    private final Function<net.minecraft.resources.Identifier, ? extends T> factory;
    private volatile @Nullable T value;

    ValueHolder(ResourceKey<R> key, Function<net.minecraft.resources.Identifier, ? extends T> factory) {
        super(key);
        this.factory = factory;
    }

    @Override
    public T value() {
        T current = this.value;
        if (current == null) {
            synchronized (this) {
                current = this.value;
                if (current == null) {
                    current = this.factory.apply(this.key.identifier());
                    this.value = current;
                }
            }
        }
        return current;
    }

    @Override
    public boolean isBound() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Holder<R> getDelegate() {
        return Holder.direct((R)this.value());
    }
}
