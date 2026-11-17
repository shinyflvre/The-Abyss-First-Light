package net.yezon.theabyss.platform.registries;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public final class LazyHolder<R> implements Holder<R> {
    private final DeferredHolder<R, ? extends R> deferred;

    LazyHolder(DeferredHolder<R, ? extends R> deferred) {
        this.deferred = deferred;
    }

    @Override
    public R value() {
        return this.deferred.value();
    }

    @Override
    public boolean isBound() {
        return this.deferred.isBound();
    }

    @Override
    public boolean is(ResourceLocation id) {
        return this.deferred.getKey().location().equals(id);
    }

    @Override
    public boolean is(ResourceKey<R> key) {
        return this.deferred.getKey().equals(key);
    }

    @Override
    public boolean is(Predicate<ResourceKey<R>> predicate) {
        return predicate.test(this.deferred.getKey());
    }

    @Override
    public boolean is(TagKey<R> tag) {
        return this.deferred.isBound() && this.deferred.getDelegate().is(tag);
    }

    @Override
    @Deprecated
    public boolean is(Holder<R> holder) {
        return holder.is(this.deferred.getKey());
    }

    @Override
    public Stream<TagKey<R>> tags() {
        return this.deferred.isBound() ? this.deferred.getDelegate().tags() : Stream.empty();
    }

    @Override
    public Either<ResourceKey<R>, R> unwrap() {
        return Either.left(this.deferred.getKey());
    }

    @Override
    public Optional<ResourceKey<R>> unwrapKey() {
        return Optional.of(this.deferred.getKey());
    }

    @Override
    public Holder.Kind kind() {
        return Holder.Kind.REFERENCE;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<R> owner) {
        return this.deferred.isBound() && this.deferred.getDelegate().canSerializeIn(owner);
    }

    public Holder<R> getDelegate() {
        return this.deferred.isBound() ? this.deferred.getDelegate() : this;
    }

    public ResourceKey<R> getKey() {
        return this.deferred.getKey();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof LazyHolder<?> other && this.deferred.getKey().equals(other.deferred.getKey());
    }

    @Override
    public int hashCode() {
        return this.deferred.getKey().hashCode();
    }

    @Override
    public String toString() {
        return "LazyHolder{" + this.deferred.getKey() + "}";
    }
}
