package net.yezon.theabyss.events;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.Gauntlet;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.SomniumUpgrades;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class ApplyRingOfFlightEvents {
    private static final double DRAIN = 0.5;
    private static final Identifier FLIGHT = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "ring_of_flight");

    private ApplyRingOfFlightEvents() {
    }

    private static boolean flightRing(ItemStack stack) {
        return stack.is(TheabyssModItems.RING_OF_FLIGHT.get()) || Gauntlet.is(stack) && Gauntlet.selectedRing(stack).is(TheabyssModItems.RING_OF_FLIGHT.get());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (flight == null) {
            return;
        }
        boolean holding = flightRing(player.getMainHandItem()) || flightRing(player.getOffhandItem());
        if (!holding) {
            flight.removeModifier(FLIGHT);
            return;
        }
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
        if (vars.Mana < DRAIN * SomniumUpgrades.costFactor(vars)) {
            flight.removeModifier(FLIGHT);
            player.sendOverlayMessage(Component.translatable("ring.theabyss.low_energy"));
            return;
        }
        if (!flight.hasModifier(FLIGHT)) {
            flight.addTransientModifier(new AttributeModifier(FLIGHT, 1.0, AttributeModifier.Operation.ADD_VALUE));
        }
        if (!player.onGround() && !player.getAbilities().flying) {
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }
        if (!player.onGround()) {
            RingVisuals.flightTick(player.level(), player);
        }
        vars.Mana -= DRAIN * SomniumUpgrades.costFactor(vars);
        vars.syncPlayerVariables(player);
    }
}
