package net.yezon.theabyss.events;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.SomniumUpgrades;

@EventBusSubscriber
public class TickHandlerEvents {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        execute(event, event.getEntity());
    }

    public static void execute(Entity entity) {
        execute(null, entity);
    }

    private static void execute(@Nullable Event event, Entity entity) {
        if (entity != null) {
            boolean somniumHandler = !entity.isInWater() && !(entity instanceof Player _player && (_player.isCreative() || _player.isSpectator()));
            TheabyssModVariables.updatePlayerVariables(entity, capability -> {
                if (!(capability.Mana > 100.0)) {
                    capability.Mana = capability.Mana + SomniumUpgrades.regenPerTick(capability.ManaRegenUpgrade);
                }
                if (capability.InfiniteSomnium) {
                    capability.Mana = 100.0;
                }
                capability.SomniumHandler = somniumHandler;
                capability.syncPlayerVariables(entity);
            });
        }
    }
}
