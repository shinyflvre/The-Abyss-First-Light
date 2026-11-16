package net.yezon.theabyss.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.player.PlayerEvent;
import net.yezon.theabyss.configuration.ConfigConfiguration;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.util.CommandUtil;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber
public class GiveBookOnFirstJoinEvents {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Entity entity = event.getEntity();
        execute(event, entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
    }

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        execute(null, world, x, y, z, entity);
    }

    private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity != null && ConfigConfiguration.GUIDE_BOOK.get() && !TheabyssModVariables.getPlayerVariables(entity).BookSpawn) {
            if (entity instanceof net.minecraft.world.entity.player.Player player && !player.level().isClientSide()) {
                net.minecraft.world.item.ItemStack book = new net.minecraft.world.item.ItemStack(net.yezon.theabyss.init.TheabyssModItems.ABYSS_GUIDE.get());
                if (!player.getInventory().add(book)) {
                    player.spawnAtLocation((net.minecraft.server.level.ServerLevel)player.level(), book);
                }
            }

            TheabyssModVariables.updatePlayerVariables(entity, capability -> {
                capability.BookSpawn = true;
                capability.syncPlayerVariables(entity);
            });
        }
    }
}
