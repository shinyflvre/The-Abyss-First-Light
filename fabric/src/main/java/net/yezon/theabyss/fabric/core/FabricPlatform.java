package net.yezon.theabyss.fabric.core;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.Platform;
import net.yezon.theabyss.platform.attachment.AttachmentType;
import org.jspecify.annotations.Nullable;

public final class FabricPlatform implements Platform {
    private static volatile @Nullable MinecraftServer server;
    private static volatile Consumer<CustomPacketPayload> clientSender = payload -> {
        throw new IllegalStateException("Cannot send to the server outside the client");
    };
    private static volatile Function<Entity, CompoundTag> persistentData = entity -> {
        throw new IllegalStateException("Persistent entity data is not wired on Fabric yet");
    };

    public FabricPlatform() {
        ServerLifecycleEvents.SERVER_STARTING.register(s -> server = s);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> server = null);
    }

    public static void setClientSender(Consumer<CustomPacketPayload> sender) {
        clientSender = sender;
    }

    public static void setPersistentData(Function<Entity, CompoundTag> provider) {
        persistentData = provider;
    }

    @Override
    public String loaderName() {
        return "fabric";
    }

    @Override
    public Dist dist() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? Dist.CLIENT : Dist.DEDICATED_SERVER;
    }

    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public @Nullable MinecraftServer currentServer() {
        return server;
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToPlayersInDimension(ServerLevel level, CustomPacketPayload payload) {
        for (ServerPlayer player : PlayerLookup.world(level)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public void sendToAllPlayers(CustomPacketPayload payload) {
        MinecraftServer current = server;
        if (current != null) {
            for (ServerPlayer player : PlayerLookup.all(current)) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    @Override
    public void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload) {
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload) {
        if (entity instanceof ServerPlayer self) {
            ServerPlayNetworking.send(self, payload);
        }
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            if (player != entity) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        clientSender.accept(payload);
    }

    @Override
    public <T> T getData(Entity holder, AttachmentType<T> type) {
        return FabricAttachments.get(holder, type);
    }

    @Override
    public <T> @Nullable T setData(Entity holder, AttachmentType<T> type, T value) {
        return FabricAttachments.set(holder, type, value);
    }

    @Override
    public boolean hasData(Entity holder, AttachmentType<?> type) {
        return FabricAttachments.has(holder, type);
    }

    @Override
    public void syncData(Entity holder, AttachmentType<?> type) {
        FabricAttachments.sync(holder, type);
    }

    @Override
    public CompoundTag persistentData(Entity entity) {
        return persistentData.apply(entity);
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    @Override
    public ServerPlayer fakePlayer(ServerLevel level) {
        return FakePlayer.get(level);
    }
}
