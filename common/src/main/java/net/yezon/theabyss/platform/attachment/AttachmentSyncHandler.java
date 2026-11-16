package net.yezon.theabyss.platform.attachment;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public interface AttachmentSyncHandler<T> {
    default boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
        return true;
    }

    void write(RegistryFriendlyByteBuf buf, T attachment, boolean initialSync);

    T read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable T previousValue);
}
