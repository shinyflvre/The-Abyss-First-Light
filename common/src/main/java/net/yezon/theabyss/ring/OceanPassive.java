package net.yezon.theabyss.ring;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.common.NeoForgeMod;
import net.yezon.theabyss.platform.event.tick.PlayerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.network.TheabyssModVariables;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class OceanPassive {
    public static final int BREATH_LEVEL = 3;
    public static final int SWIM_LEVEL = 4;
    private static final Identifier SWIM = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "ocean_ring_swim");

    private OceanPassive() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        boolean check = player.tickCount % 10 == 0;
        int usage = SomniumUpgrades.level(TheabyssModVariables.getPlayerVariables(player), SomniumUpgrades.Kind.USAGE);
        AttributeInstance swim = player.getAttribute(NeoForgeMod.SWIM_SPEED);
        if (check) {
            boolean has = usage >= BREATH_LEVEL && carries(player);
            net.yezon.theabyss.platform.PersistentData.get(player).putBoolean("theabyss_ocean_passive", has);
            boolean fast = has && usage >= SWIM_LEVEL;
            if (swim != null) {
                if (fast && !swim.hasModifier(SWIM)) {
                    swim.addTransientModifier(new AttributeModifier(SWIM, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                } else if (!fast && swim.hasModifier(SWIM)) {
                    swim.removeModifier(SWIM);
                }
            }
        }
        if (net.yezon.theabyss.platform.PersistentData.get(player).getBooleanOr("theabyss_ocean_passive", false) && player.getAirSupply() < player.getMaxAirSupply()) {
            player.setAirSupply(player.getMaxAirSupply());
        }
    }

    private static boolean carries(ServerPlayer player) {
        for (ItemStack stack : player.getInventory()) {
            if (stack.is(TheabyssModItems.RING_OF_OCEAN.get())) {
                return true;
            }
            if (Gauntlet.is(stack) && Gauntlet.rings(stack).stream().anyMatch(r -> r.is(TheabyssModItems.RING_OF_OCEAN.get()))) {
                return true;
            }
        }
        return false;
    }
}
