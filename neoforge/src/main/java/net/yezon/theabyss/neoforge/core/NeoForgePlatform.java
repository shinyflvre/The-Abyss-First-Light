package net.yezon.theabyss.neoforge.core;

import java.nio.file.Path;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.Platform;
import net.yezon.theabyss.platform.attachment.AttachmentType;
import org.jspecify.annotations.Nullable;

public final class NeoForgePlatform implements Platform {
    @Override
    public String loaderName() {
        return "neoforge";
    }

    @Override
    public Dist dist() {
        return FMLEnvironment.dist.isClient() ? Dist.CLIENT : Dist.DEDICATED_SERVER;
    }

    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public @Nullable MinecraftServer currentServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToPlayersInDimension(ServerLevel level, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersInDimension(level, payload);
    }

    @Override
    public void sendToAllPlayers(CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    @Override
    public void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    @Override
    public void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    @Override
    public <T> T getData(Entity holder, AttachmentType<T> type) {
        return holder.getData(NeoForgeAttachments.nativeType(type));
    }

    @Override
    public <T> @Nullable T setData(Entity holder, AttachmentType<T> type, T value) {
        return holder.setData(NeoForgeAttachments.nativeType(type), value);
    }

    @Override
    public boolean hasData(Entity holder, AttachmentType<?> type) {
        return holder.hasData(NeoForgeAttachments.nativeType(type));
    }

    @Override
    public void syncData(Entity holder, AttachmentType<?> type) {
        holder.syncData(NeoForgeAttachments.nativeType(type));
    }

    @Override
    public CompoundTag persistentData(Entity entity) {
        return entity.getPersistentData();
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    @Override
    public ServerPlayer fakePlayer(ServerLevel level) {
        return FakePlayerFactory.getMinecraft(level);
    }
}
