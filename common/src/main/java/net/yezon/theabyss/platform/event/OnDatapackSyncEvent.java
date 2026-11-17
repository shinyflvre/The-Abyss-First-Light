package net.yezon.theabyss.platform.event;

import org.jspecify.annotations.Nullable;

public abstract class OnDatapackSyncEvent extends Event {
    public abstract net.minecraft.server.players.PlayerList getPlayerList();

    public abstract net.minecraft.server.level.@Nullable ServerPlayer getPlayer();
}
