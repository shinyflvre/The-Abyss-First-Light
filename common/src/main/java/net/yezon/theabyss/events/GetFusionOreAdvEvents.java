package net.yezon.theabyss.events;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class GetFusionOreAdvEvents {
    public static void execute(Entity entity) {
        if (entity instanceof ServerPlayer _player) {
            award(_player, "theabyss:ore_adv");
            award(_player, "theabyss:fusion_ore_adv");
        }
    }

    private static void award(ServerPlayer player, String id) {
        AdvancementHolder _adv = player.level().getServer().getAdvancements().get(Identifier.parse(id));
        if (_adv != null) {
            AdvancementProgress _ap = player.getAdvancements().getOrStartProgress(_adv);
            if (!_ap.isDone()) {
                for (String criteria : _ap.getRemainingCriteria()) {
                    player.getAdvancements().award(_adv, criteria);
                }
            }
        }
    }
}
