package net.yezon.theabyss.fabric.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.network.TheabyssModNetwork;
import net.yezon.theabyss.platform.network.event.RegisterPayloadHandlersEvent;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.platform.network.handling.IPayloadHandler;
import net.yezon.theabyss.platform.network.registration.PayloadRegistrar;
import org.jspecify.annotations.Nullable;

public final class FabricNetwork {
    private static final List<ClientReceiver<?>> CLIENT_RECEIVERS = new ArrayList<>();
    private static boolean registered;

    private FabricNetwork() {
    }

    public static void registerCommon() {
        if (registered) {
            return;
        }
        registered = true;
        TheabyssModNetwork.register(new RegisterPayloadHandlersEvent() {
            @Override
            public PayloadRegistrar registrar(String version) {
                return new Registrar();
            }
        });
        FabricAttachments.registerPayloads();
    }

    public static List<ClientReceiver<?>> clientReceivers() {
        return List.copyOf(CLIENT_RECEIVERS);
    }

    public record ClientReceiver<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, IPayloadHandler<T> handler) {
    }

    public record Context(Player player, Function<Runnable, CompletableFuture<Void>> executor) implements IPayloadContext {
        @Override
        public CompletableFuture<Void> enqueueWork(Runnable task) {
            return this.executor.apply(task);
        }
    }

    private static <T extends CustomPacketPayload> void serverReceiver(CustomPacketPayload.Type<T> type, IPayloadHandler<T> handler) {
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
            ServerPlayer player = context.player();
            handler.handle(payload, new Context(player, task -> context.server().submit(task)));
        });
    }

    private static final class Registrar extends PayloadRegistrar {
        @Override
        public <T extends CustomPacketPayload> PayloadRegistrar playToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            IPayloadHandler<T> handler) {
            PayloadTypeRegistry.clientboundPlay().register(type, codec);
            CLIENT_RECEIVERS.add(new ClientReceiver<>(type, handler));
            return this;
        }

        @Override
        public <T extends CustomPacketPayload> PayloadRegistrar playToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            IPayloadHandler<T> handler) {
            PayloadTypeRegistry.serverboundPlay().register(type, codec);
            serverReceiver(type, handler);
            return this;
        }

        @Override
        public <T extends CustomPacketPayload> PayloadRegistrar playBidirectional(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            IPayloadHandler<T> serverHandler, @Nullable IPayloadHandler<T> clientHandler) {
            PayloadTypeRegistry.clientboundPlay().register(type, codec);
            PayloadTypeRegistry.serverboundPlay().register(type, codec);
            serverReceiver(type, serverHandler);
            if (clientHandler != null) {
                CLIENT_RECEIVERS.add(new ClientReceiver<>(type, clientHandler));
            }
            return this;
        }

        @Override
        public PayloadRegistrar versioned(String version) {
            return this;
        }
    }
}
