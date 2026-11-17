package net.yezon.theabyss.util;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class CommandUtil {
    private CommandUtil() {
    }

    public static CommandSourceStack source(
        CommandSource source,
        Vec3 position,
        Vec2 rotation,
        ServerLevel level,
        int permissionLevel,
        String textName,
        Component displayName,
        MinecraftServer server,
        @Nullable Entity entity
    ) {
        if (entity != null) {
            return new CommandSourceStack(source, position, rotation, level, permissionLevel, entity.getName().getString(), entity.getDisplayName(), server, entity);
        }
        return new CommandSourceStack(source, position, rotation, level, permissionLevel, displayName.getString(), displayName, server, null);
    }

    public static void run(ServerLevel level, Vec3 position, String command) {
        level.getServer().getCommands().performPrefixedCommand(
            source(CommandSource.NULL, position, Vec2.ZERO, level, 4, "", Component.literal(""), level.getServer(), null).withSuppressedOutput(),
            command
        );
    }
}
