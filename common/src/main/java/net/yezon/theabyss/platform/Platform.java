package net.yezon.theabyss.platform;

import java.nio.file.Path;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.platform.attachment.AttachmentType;
import org.jspecify.annotations.Nullable;

public interface Platform {
    static Platform get() {
        Platform platform = Holder.instance;
        if (platform == null) {
            throw new IllegalStateException("The Abyss platform layer was not installed by the loader");
        }
        return platform;
    }

    static void install(Platform platform) {
        Holder.instance = platform;
    }

    String loaderName();

    Dist dist();

    Path configDir();

    boolean isModLoaded(String modId);

    @Nullable MinecraftServer currentServer();

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    void sendToPlayersInDimension(ServerLevel level, CustomPacketPayload payload);

    void sendToAllPlayers(CustomPacketPayload payload);

    void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload);

    void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload);

    void sendToServer(CustomPacketPayload payload);

    <T> T getData(Entity holder, AttachmentType<T> type);

    <T> @Nullable T setData(Entity holder, AttachmentType<T> type, T value);

    boolean hasData(Entity holder, AttachmentType<?> type);

    void syncData(Entity holder, AttachmentType<?> type);

    CompoundTag persistentData(Entity entity);

    boolean isFakePlayer(Player player);

    ServerPlayer fakePlayer(ServerLevel level);

    final class Holder {
        private static volatile @Nullable Platform instance;

        private Holder() {
        }
    }
}
