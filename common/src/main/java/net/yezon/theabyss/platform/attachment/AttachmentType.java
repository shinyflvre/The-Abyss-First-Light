package net.yezon.theabyss.platform.attachment;

import com.mojang.serialization.MapCodec;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.yezon.theabyss.compat.ValueInput;
import net.yezon.theabyss.compat.ValueOutput;
import net.yezon.theabyss.platform.common.util.ValueIOSerializable;
import org.jspecify.annotations.Nullable;

public final class AttachmentType<T> {
    private final Function<IAttachmentHolder, T> defaultValue;
    private final @Nullable Serializer<T> serializer;
    private final boolean copyOnDeath;
    private final @Nullable AttachmentSyncHandler<T> syncHandler;

    private AttachmentType(Builder<T> builder) {
        this.defaultValue = builder.defaultValue;
        this.serializer = builder.serializer;
        this.copyOnDeath = builder.copyOnDeath;
        this.syncHandler = builder.syncHandler;
    }

    public static <T> Builder<T> builder(Supplier<T> defaultValueSupplier) {
        return new Builder<>(holder -> defaultValueSupplier.get());
    }

    public static <T> Builder<T> builder(Function<IAttachmentHolder, T> defaultValueConstructor) {
        return new Builder<>(defaultValueConstructor);
    }

    public static <T extends ValueIOSerializable> Builder<T> serializable(Supplier<T> defaultValueSupplier) {
        Builder<T> builder = builder(defaultValueSupplier);
        builder.serializer = new Serializer<>() {
            @Override
            public T read(IAttachmentHolder holder, ValueInput input) {
                T value = defaultValueSupplier.get();
                value.deserialize(input);
                return value;
            }

            @Override
            public boolean write(T attachment, ValueOutput output) {
                attachment.serialize(output);
                return true;
            }
        };
        return builder;
    }

    public T createDefault(IAttachmentHolder holder) {
        return this.defaultValue.apply(holder);
    }

    public @Nullable Serializer<T> serializer() {
        return this.serializer;
    }

    public boolean copyOnDeath() {
        return this.copyOnDeath;
    }

    public @Nullable AttachmentSyncHandler<T> syncHandler() {
        return this.syncHandler;
    }

    public interface Serializer<T> {
        T read(IAttachmentHolder holder, ValueInput input);

        boolean write(T attachment, ValueOutput output);
    }

    public static final class Builder<T> {
        private final Function<IAttachmentHolder, T> defaultValue;
        private @Nullable Serializer<T> serializer;
        private boolean copyOnDeath;
        private @Nullable AttachmentSyncHandler<T> syncHandler;

        private Builder(Function<IAttachmentHolder, T> defaultValue) {
            this.defaultValue = defaultValue;
        }

        public Builder<T> serialize(MapCodec<T> codec) {
            return this.serialize(codec, value -> true);
        }

        public Builder<T> serialize(MapCodec<T> codec, Predicate<? super T> shouldSerialize) {
            this.serializer = new Serializer<>() {
                @Override
                public T read(IAttachmentHolder holder, ValueInput input) {
                    return codec.codec().parse(NbtOps.INSTANCE, input.tag()).result().orElseThrow();
                }

                @Override
                public boolean write(T attachment, ValueOutput output) {
                    if (!shouldSerialize.test(attachment)) {
                        return false;
                    }
                    codec.codec().encodeStart(NbtOps.INSTANCE, attachment).result()
                        .ifPresent(encoded -> output.tag().merge((CompoundTag)encoded));
                    return true;
                }
            };
            return this;
        }

        public Builder<T> copyOnDeath() {
            this.copyOnDeath = true;
            return this;
        }

        public Builder<T> sync(AttachmentSyncHandler<T> syncHandler) {
            this.syncHandler = syncHandler;
            return this;
        }

        public Builder<T> sync(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
            return this.sync((holder, to) -> true, streamCodec);
        }

        public Builder<T> sync(BiPredicate<IAttachmentHolder, ServerPlayer> sendToPlayer, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
            this.syncHandler = new AttachmentSyncHandler<>() {
                @Override
                public boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
                    return sendToPlayer.test(holder, to);
                }

                @Override
                public void write(RegistryFriendlyByteBuf buf, T attachment, boolean initialSync) {
                    streamCodec.encode(buf, attachment);
                }

                @Override
                public T read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable T previousValue) {
                    return streamCodec.decode(buf);
                }
            };
            return this;
        }

        public AttachmentType<T> build() {
            return new AttachmentType<>(this);
        }
    }
}
