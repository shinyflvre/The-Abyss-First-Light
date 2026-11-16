package net.yezon.theabyss.events;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.network.TheabyssModVariables;

public class EnchantedBottleOfManaDrinkedEvents {
    public static void execute(Entity entity) {
        if (entity == null) {
            return;
        }

        double _mana = TheabyssModVariables.getPlayerVariables(entity).Mana;
        if (_mana > 81.0) {
            if (entity instanceof Player _player && !_player.level().isClientSide()) {
                _player.sendOverlayMessage(Component.translatable("event.theabyss.no_effect"));
            }
        } else {
            TheabyssModVariables.updatePlayerVariables(entity, capability -> {
                capability.Mana = _mana + 50.0;
                capability.syncPlayerVariables(entity);
            });
        }
    }
}
