package net.yezon.theabyss.events;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class GetSomniumAdvEvents {
    public static void execute(Entity entity) {
        if (entity instanceof ServerPlayer _player) {
            award(_player, "theabyss:magic_adv");
            award(_player, "theabyss:somnium_adv");
        }
    }

    private static void award(ServerPlayer player, String id) {
        AdvancementHolder _adv = player.level().getServer().getAdvancements().get(ResourceLocation.parse(id));
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
