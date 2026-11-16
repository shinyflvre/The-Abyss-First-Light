package net.yezon.theabyss.entity;

import java.util.Map;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class MiniBossNames {
    private static final Map<Class<? extends Entity>, String> OLD_NAMES = Map.of(TheRokaEntity.class, "Roka", AbyssaurEntity.class, "Abyssaur",
        MagicianEntity.class, "Magician", ElderEntity.class, "Elder", CrystalGolemEntity.class, "Crystal Golem");

    private MiniBossNames() {
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (entity instanceof TheRokaEntity roka) {
            var speed = roka.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
            if (speed != null && speed.getBaseValue() > TheRokaEntity.SPEED + 1.0E-4) {
                speed.setBaseValue(TheRokaEntity.SPEED);
            }
        }
        if (!entity.hasCustomName()) {
            return;
        }
        String old = OLD_NAMES.get(entity.getClass());
        if (old != null && entity.getCustomName() != null && old.equals(entity.getCustomName().getString())) {
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
        }
    }
}
