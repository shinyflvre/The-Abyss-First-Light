package net.yezon.theabyss.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class ImmortalPotionActiveEvents {
    public static void execute(Entity entity) {
        if (entity != null) {
            if (entity instanceof Player _player && !_player.isCreative() && !_player.isSpectator()) {
                _player.getAbilities().invulnerable = true;
                _player.onUpdateAbilities();
            }
        }
    }
}
