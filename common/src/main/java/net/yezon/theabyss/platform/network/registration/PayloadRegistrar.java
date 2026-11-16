package net.yezon.theabyss.platform.network.registration;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.yezon.theabyss.platform.network.handling.IPayloadHandler;
import org.jspecify.annotations.Nullable;

public abstract class PayloadRegistrar {
    public abstract <T extends CustomPacketPayload> PayloadRegistrar playToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
        IPayloadHandler<T> handler);

    public abstract <T extends CustomPacketPayload> PayloadRegistrar playToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
        IPayloadHandler<T> handler);

    public abstract <T extends CustomPacketPayload> PayloadRegistrar playBidirectional(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
        IPayloadHandler<T> serverHandler, @Nullable IPayloadHandler<T> clientHandler);

    public <T extends CustomPacketPayload> PayloadRegistrar playBidirectional(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
        IPayloadHandler<T> handler) {
        return this.playBidirectional(type, codec, handler, handler);
    }

    public abstract PayloadRegistrar versioned(String version);
}
