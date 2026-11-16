package net.yezon.theabyss.events;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class TheAbyssAdvGrantEvents {
    public static void execute(Entity entity) {
        if (entity instanceof ServerPlayer _player) {
            grant(_player, "theabyss:the_abyss_adv");
        }
    }

    static void grant(ServerPlayer _player, String id) {
        AdvancementHolder _adv = _player.level().getServer().getAdvancements().get(Identifier.parse(id));
        if (_adv != null) {
            AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
            if (!_ap.isDone()) {
                for (String criteria : _ap.getRemainingCriteria()) {
                    _player.getAdvancements().award(_adv, criteria);
                }
            }
        }
    }
}
