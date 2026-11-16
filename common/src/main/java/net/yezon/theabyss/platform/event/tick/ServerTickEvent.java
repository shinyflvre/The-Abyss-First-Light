package net.yezon.theabyss.platform.event.tick;

import net.yezon.theabyss.platform.event.Event;

public abstract class ServerTickEvent extends Event {

    public static class Post extends ServerTickEvent {
        private final net.minecraft.server.MinecraftServer server;

        public Post(net.minecraft.server.MinecraftServer server) {
            this.server = server;
        }

        public net.minecraft.server.MinecraftServer getServer() {
            return this.server;
        }
    }

    public static class Pre extends ServerTickEvent {
    }
}
