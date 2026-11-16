package net.yezon.theabyss.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class ImmortalPotionActiveEvents {
    public static void execute(Entity entity) {
        if (entity != null) {
            if (entity instanceof Player _player && (_player.gameMode() == GameType.SURVIVAL || _player.gameMode() == GameType.ADVENTURE)) {
                _player.getAbilities().invulnerable = true;
                _player.onUpdateAbilities();
            }
        }
    }
}
