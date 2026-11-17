package net.yezon.theabyss.events;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class GetLoranEnergyAdvEvents {
    public static void execute(Entity entity) {
        if (entity instanceof ServerPlayer _player && isDone(_player, "theabyss:loran_adv")) {
            award(_player, "theabyss:ore_adv");
            award(_player, "theabyss:powered_loran_adv");
        }
    }

    private static boolean isDone(ServerPlayer player, String id) {
        AdvancementHolder _adv = player.level().getServer().getAdvancements().get(ResourceLocation.parse(id));
        return _adv != null && player.getAdvancements().getOrStartProgress(_adv).isDone();
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
