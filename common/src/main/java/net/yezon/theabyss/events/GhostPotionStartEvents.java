package net.yezon.theabyss.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;

public class GhostPotionStartEvents {
    public static final String SAVED_MODE = "theabyss_ghost_mode";

    public static void execute(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            CompoundTag data = net.yezon.theabyss.platform.PersistentData.get(player);
            GameType current = player.gameMode.getGameModeForPlayer();
            if (!data.contains(SAVED_MODE) && current != GameType.SPECTATOR) {
                data.putString(SAVED_MODE, current.getName());
            }
            player.setGameMode(GameType.SPECTATOR);
        }
    }
}
