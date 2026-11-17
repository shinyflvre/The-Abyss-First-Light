package net.yezon.theabyss.fabric.core;

import io.netty.buffer.Unpooled;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.compat.ValueInput;
import net.yezon.theabyss.compat.ValueOutput;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.platform.attachment.AttachmentSyncHandler;
import net.yezon.theabyss.platform.attachment.AttachmentType;
import net.yezon.theabyss.platform.attachment.IAttachmentHolder;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

public final class FabricAttachments {
    public static final String SAVE_KEY = "theabyss:attachments";
    private static final Map<ResourceLocation, AttachmentType<?>> BY_ID = new LinkedHashMap<>();
    private static final Map<AttachmentType<?>, ResourceLocation> IDS = new LinkedHashMap<>();

    private FabricAttachments() {
    }

    public static void register(DeferredRegister<AttachmentType<?>> register) {
        for (DeferredHolder<AttachmentType<?>, ? extends AttachmentType<?>> holder : register.getEntries()) {
            AttachmentType<?> type = holder.get();
            BY_ID.put(holder.getId(), type);
            IDS.put(type, holder.getId());
        }
        register.markRegistered();
    }

    static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(SyncPayload.TYPE, SyncPayload.STREAM_CODEC);
        EntityTrackingEvents.START_TRACKING.register((entity, player) -> syncAll(entity, player, true));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> syncAll(handler.getPlayer(), handler.getPlayer(), true));
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> copy(oldPlayer, newPlayer, alive));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> syncAll(newPlayer, newPlayer, true));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> syncAll(player, player, true));
    }

    private static Map<AttachmentType<?>, Object> storage(Entity entity) {
        return ((AttachmentStorage)entity).theabyss$attachments();
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Entity holder, AttachmentType<T> type) {
        Map<AttachmentType<?>, Object> map = storage(holder);
        Object value = map.get(type);
        if (value == null) {
            value = type.createDefault((IAttachmentHolder)holder);
            map.put(type, value);
        }
        return (T)value;
    }

    @SuppressWarnings("unchecked")
    public static <T> @Nullable T set(Entity holder, AttachmentType<T> type, T value) {
        T previous = (T)storage(holder).put(type, value);
        sync(holder, type);
        return previous;
    }

    public static boolean has(Entity holder, AttachmentType<?> type) {
        return storage(holder).containsKey(type);
    }

    public static void sync(Entity holder, AttachmentType<?> type) {
        if (!(holder.level() instanceof ServerLevel) || type.syncHandler() == null || !storage(holder).containsKey(type)) {
            return;
        }
        if (holder instanceof ServerPlayer self) {
            send(holder, type, self, false);
        }
        for (ServerPlayer player : net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(holder)) {
            if (player != holder) {
                send(holder, type, player, false);
            }
        }
    }

    private static void syncAll(Entity holder, ServerPlayer to, boolean initial) {
        for (AttachmentType<?> type : storage(holder).keySet()) {
            if (type.syncHandler() != null) {
                send(holder, type, to, initial);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void send(Entity holder, AttachmentType<T> type, ServerPlayer to, boolean initial) {
        AttachmentSyncHandler<T> handler = type.syncHandler();
        ResourceLocation id = IDS.get(type);
        if (handler == null || id == null || !handler.sendToPlayer((IAttachmentHolder)holder, to)) {
            return;
        }
        T value = (T)storage(holder).get(type);
        if (value == null) {
            return;
        }
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), holder.registryAccess());
        try {
            handler.write(buf, value, initial);
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            ServerPlayNetworking.send(to, new SyncPayload(holder.getId(), id, data));
        } finally {
            buf.release();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void receive(Entity holder, ResourceLocation id, byte[] data, RegistryAccess access) {
        AttachmentType<T> type = (AttachmentType<T>)BY_ID.get(id);
        if (type == null || type.syncHandler() == null) {
            TheabyssMod.LOGGER.warn("Received sync for unknown attachment {}", id);
            return;
        }
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), access);
        try {
            T previous = (T)storage(holder).get(type);
            T value = type.syncHandler().read((IAttachmentHolder)holder, buf, previous);
            if (value == null) {
                storage(holder).remove(type);
            } else {
                storage(holder).put(type, value);
            }
        } finally {
            buf.release();
        }
    }

    @SuppressWarnings("unchecked")
    public static void save(Entity holder, ValueOutput output) {
        Map<AttachmentType<?>, Object> map = storage(holder);
        if (map.isEmpty()) {
            return;
        }
        ValueOutput all = output.child(SAVE_KEY);
        for (Map.Entry<AttachmentType<?>, Object> entry : map.entrySet()) {
            AttachmentType<Object> type = (AttachmentType<Object>)entry.getKey();
            AttachmentType.Serializer<Object> serializer = type.serializer();
            ResourceLocation id = IDS.get(type);
            if (serializer == null || id == null) {
                continue;
            }
            String key = id.toString();
            if (!serializer.write(entry.getValue(), all.child(key))) {
                all.discard(key);
            }
        }
        if (all.isEmpty()) {
            output.discard(SAVE_KEY);
        }
    }

    public static void load(Entity holder, ValueInput input) {
        input.child(SAVE_KEY).ifPresent(all -> {
            for (Map.Entry<ResourceLocation, AttachmentType<?>> entry : BY_ID.entrySet()) {
                AttachmentType.Serializer<?> serializer = entry.getValue().serializer();
                if (serializer == null) {
                    continue;
                }
                all.child(entry.getKey().toString()).ifPresent(child -> {
                    Object value = serializer.read((IAttachmentHolder)holder, child);
                    if (value != null) {
                        storage(holder).put(entry.getValue(), value);
                    }
                });
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static void copy(ServerPlayer from, ServerPlayer to, boolean alive) {
        for (Map.Entry<AttachmentType<?>, Object> entry : storage(from).entrySet()) {
            AttachmentType<Object> type = (AttachmentType<Object>)entry.getKey();
            AttachmentType.Serializer<Object> serializer = type.serializer();
            if (serializer == null || !(alive || type.copyOnDeath())) {
                continue;
            }
            CompoundTag tag = new CompoundTag();
            if (serializer.write(entry.getValue(), ValueOutput.of(tag, from.registryAccess()))) {
                storage(to).put(type, serializer.read((IAttachmentHolder)to, ValueInput.of(tag, to.registryAccess())));
            }
        }
    }

    public record SyncPayload(int entityId, ResourceLocation attachment, byte[] data) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "attachment_sync"));
        public static final StreamCodec<FriendlyByteBuf, SyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncPayload::entityId,
            ResourceLocation.STREAM_CODEC, SyncPayload::attachment,
            ByteBufCodecs.BYTE_ARRAY, SyncPayload::data,
            SyncPayload::new);

        @Override
        public CustomPacketPayload.Type<SyncPayload> type() {
            return TYPE;
        }
    }
}
