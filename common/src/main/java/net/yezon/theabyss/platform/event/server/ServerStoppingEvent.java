package net.yezon.theabyss.platform.event.server;

import net.yezon.theabyss.platform.event.Event;

public class ServerStoppingEvent extends Event {
    private final net.minecraft.server.MinecraftServer server;

    public ServerStoppingEvent(net.minecraft.server.MinecraftServer server) {
        this.server = server;
    }

    public net.minecraft.server.MinecraftServer getServer() {
        return this.server;
    }
}
