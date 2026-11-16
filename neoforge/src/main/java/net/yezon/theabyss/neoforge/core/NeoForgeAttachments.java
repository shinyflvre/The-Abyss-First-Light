package net.yezon.theabyss.neoforge.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yezon.theabyss.platform.attachment.AttachmentSyncHandler;
import net.yezon.theabyss.platform.attachment.AttachmentType;
import net.yezon.theabyss.platform.attachment.IAttachmentHolder;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

public final class NeoForgeAttachments {
    private static final Map<AttachmentType<?>, net.neoforged.neoforge.attachment.AttachmentType<?>> NATIVE = new ConcurrentHashMap<>();

    private NeoForgeAttachments() {
    }

    @SuppressWarnings("unchecked")
    public static <T> net.neoforged.neoforge.attachment.AttachmentType<T> nativeType(AttachmentType<T> type) {
        net.neoforged.neoforge.attachment.AttachmentType<?> found = NATIVE.get(type);
        if (found == null) {
            throw new IllegalStateException("Attachment type was not registered on NeoForge: " + type);
        }
        return (net.neoforged.neoforge.attachment.AttachmentType<T>)found;
    }

    public static void register(DeferredRegister<AttachmentType<?>> common, IEventBus bus) {
        net.neoforged.neoforge.registries.DeferredRegister<net.neoforged.neoforge.attachment.AttachmentType<?>> neo =
            net.neoforged.neoforge.registries.DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, common.getNamespace());
        for (DeferredHolder<AttachmentType<?>, ? extends AttachmentType<?>> holder : common.getEntries()) {
            neo.register(holder.getId().getPath(), () -> toNative(holder.get()));
        }
        common.markRegistered();
        neo.register(bus);
    }

    private static <T> net.neoforged.neoforge.attachment.AttachmentType<T> toNative(AttachmentType<T> type) {
        net.neoforged.neoforge.attachment.AttachmentType.Builder<T> builder =
            net.neoforged.neoforge.attachment.AttachmentType.builder(holder -> type.createDefault(holder(holder)));
        AttachmentType.Serializer<T> serializer = type.serializer();
        if (serializer != null) {
            builder.serialize(new IAttachmentSerializer<>() {
                @Override
                public T read(net.neoforged.neoforge.attachment.IAttachmentHolder holder, ValueInput input) {
                    return serializer.read(holder(holder), input);
                }

                @Override
                public boolean write(T attachment, ValueOutput output) {
                    return serializer.write(attachment, output);
                }
            });
        }
        if (type.copyOnDeath()) {
            builder.copyOnDeath();
        }
        AttachmentSyncHandler<T> sync = type.syncHandler();
        if (sync != null) {
            builder.sync(new net.neoforged.neoforge.attachment.AttachmentSyncHandler<>() {
                @Override
                public boolean sendToPlayer(net.neoforged.neoforge.attachment.IAttachmentHolder holder, ServerPlayer to) {
                    return sync.sendToPlayer(holder(holder), to);
                }

                @Override
                public void write(RegistryFriendlyByteBuf buf, T attachment, boolean initialSync) {
                    sync.write(buf, attachment, initialSync);
                }

                @Override
                public T read(net.neoforged.neoforge.attachment.IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable T previousValue) {
                    return sync.read(holder(holder), buf, previousValue);
                }
            });
        }
        net.neoforged.neoforge.attachment.AttachmentType<T> built = builder.build();
        NATIVE.put(type, built);
        return built;
    }

    private static IAttachmentHolder holder(net.neoforged.neoforge.attachment.IAttachmentHolder holder) {
        return (IAttachmentHolder)(Object)holder;
    }
}
