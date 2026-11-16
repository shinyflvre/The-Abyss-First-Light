package net.yezon.theabyss.events;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yezon.theabyss.configuration.ConfigConfiguration;
import net.yezon.theabyss.network.TheabyssModVariables;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber
public class ConfigurationEvents {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        execute(event, event.getEntity());
    }

    public static void execute(Entity entity) {
        execute(null, entity);
    }

    private static void execute(@Nullable Event event, Entity entity) {
        if (entity != null) {
            double _hudX = ConfigConfiguration.SOMNIUM_XPOS.get();
            double _hudY = ConfigConfiguration.SOMNIUM_YPOS.get();
            TheabyssModVariables.updatePlayerVariables(entity, capability -> {
                if (capability.HudX != _hudX || capability.HudY != _hudY) {
                    capability.HudX = _hudX;
                    capability.HudY = _hudY;
                    capability.syncPlayerVariables(entity);
                }
            });
        }
    }
}
