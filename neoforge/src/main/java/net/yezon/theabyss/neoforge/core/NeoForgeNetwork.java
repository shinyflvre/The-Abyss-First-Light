package net.yezon.theabyss.neoforge.core;

import java.util.concurrent.CompletableFuture;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.yezon.theabyss.network.TheabyssModNetwork;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.platform.network.handling.IPayloadHandler;
import net.yezon.theabyss.platform.network.registration.PayloadRegistrar;
import org.jspecify.annotations.Nullable;

public final class NeoForgeNetwork {
    private NeoForgeNetwork() {
    }

    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        TheabyssModNetwork.register(new net.yezon.theabyss.platform.network.event.RegisterPayloadHandlersEvent() {
            @Override
            public PayloadRegistrar registrar(String version) {
                return new Registrar(event.registrar(version));
            }
        });
    }

    private static <T extends CustomPacketPayload> net.neoforged.neoforge.network.handling.IPayloadHandler<T> wrap(IPayloadHandler<T> handler) {
        return (payload, context) -> handler.handle(payload, new Context(context));
    }

    private record Context(net.neoforged.neoforge.network.handling.IPayloadContext context) implements IPayloadContext {
        @Override
        public Player player() {
            return this.context.player();
        }

        @Override
        public CompletableFuture<Void> enqueueWork(Runnable task) {
            return this.context.enqueueWork(task);
        }
    }

    private static final class Registrar extends PayloadRegistrar {
        private net.neoforged.neoforge.network.registration.PayloadRegistrar registrar;

        Registrar(net.neoforged.neoforge.network.registration.PayloadRegistrar registrar) {
            this.registrar = registrar;
        }

        @Override
        public <T extends CustomPacketPayload> PayloadRegistrar playToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            IPayloadHandler<T> handler) {
            this.registrar = this.registrar.playToClient(type, codec, wrap(handler));
            return this;
        }

        @Override
        public <T extends CustomPacketPayload> PayloadRegistrar playToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            IPayloadHandler<T> handler) {
            this.registrar = this.registrar.playToServer(type, codec, wrap(handler));
            return this;
        }

        @Override
        public <T extends CustomPacketPayload> PayloadRegistrar playBidirectional(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            IPayloadHandler<T> serverHandler, @Nullable IPayloadHandler<T> clientHandler) {
            this.registrar = this.registrar.playBidirectional(type, codec, clientHandler == null ? wrap(serverHandler)
                : new DirectionalPayloadHandler<>(wrap(clientHandler), wrap(serverHandler)));
            return this;
        }

        @Override
        public PayloadRegistrar versioned(String version) {
            this.registrar = this.registrar.versioned(version);
            return this;
        }
    }
}
