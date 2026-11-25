package net.yezon.theabyss.ring;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.yezon.theabyss.events.ApplyRingOfCrystalsEvents;
import net.yezon.theabyss.init.TheabyssModItems;
import org.jspecify.annotations.Nullable;

public final class RingMagic {
    private RingMagic() {
    }

    public static @Nullable InteractionHand magicHand(Player player) {
        if (Gauntlet.isRing(player.getMainHandItem()) || Gauntlet.is(player.getMainHandItem())) {
            return InteractionHand.MAIN_HAND;
        }
        if (Gauntlet.isRing(player.getOffhandItem()) || Gauntlet.is(player.getOffhandItem())) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    public static InteractionHand swingHand(net.minecraft.world.entity.LivingEntity entity) {
        InteractionHand hand = entity instanceof Player player ? magicHand(player) : null;
        return hand == null ? InteractionHand.MAIN_HAND : hand;
    }

    public static void main(ServerPlayer player) {
        InteractionHand hand = magicHand(player);
        if (hand == null) {
            net.yezon.theabyss.compat.ObscureSkillsCompat.castSelected(player);
            return;
        }
        ItemStack held = player.getItemInHand(hand);
        if (Gauntlet.is(held)) {
            castFromGauntlet(player, hand, held, true);
        } else if (!player.getCooldowns().isOnCooldown(held)) {
            InteractionResult result = held.getItem().use(player.level(), player, hand);
            if (result instanceof InteractionResult.Success) {
                player.swing(hand, SwingAnimation.DEFAULT, true);
            }
        }
    }

    public static void castFromGauntlet(Player player, InteractionHand hand, ItemStack gauntlet, boolean swing) {
        if (player.level().isClientSide()) {
            return;
        }
        if (net.yezon.theabyss.compat.ObscureSkillsCompat.castSelected(player)) {
            return;
        }
        ItemStack ring = Gauntlet.selectedRing(gauntlet);
        if (ring.isEmpty()) {
            if (player instanceof ServerPlayer sp) {
                sp.sendOverlayMessage(Component.translatable("gauntlet.theabyss.empty_slot"));
            }
            return;
        }
        if (player.getCooldowns().isOnCooldown(ring)) {
            return;
        }
        Gauntlet.withSelectedRing(gauntlet, selected -> {
            player.setItemInHand(hand, selected);
            ItemStack after;
            try {
                InteractionResult result = selected.getItem().use(player.level(), player, hand);
                if (swing && result instanceof InteractionResult.Success) {
                    player.swing(hand, SwingAnimation.DEFAULT, true);
                }
                if (result instanceof InteractionResult.Success && player instanceof ServerPlayer caster) {
                    net.yezon.theabyss.rift.RiftStory.onGauntletCast(caster);
                }
            } finally {
                after = player.getItemInHand(hand);
                player.setItemInHand(hand, gauntlet);
            }
            return after;
        });
    }

    public static void secondary(ServerPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack ring = player.getItemInHand(hand);
            if (hasSecondary(ring)) {
                secondary(player, ring);
                return;
            }
        }
        ItemStack gauntlet = Gauntlet.held(player);
        if (gauntlet.isEmpty() || net.yezon.theabyss.compat.ObscureSkillsCompat.secondaryBlocked(player)) {
            return;
        }
        if (hasSecondary(Gauntlet.selectedRing(gauntlet))) {
            Gauntlet.withSelectedRing(gauntlet, selected -> {
                secondary(player, selected);
                return selected;
            });
            net.yezon.theabyss.rift.RiftStory.onGauntletCast(player);
        } else {
            player.sendOverlayMessage(Component.translatable("gauntlet.theabyss.no_second_magic"));
        }
    }

    private static boolean hasSecondary(ItemStack ring) {
        return ring.is(TheabyssModItems.RING_OF_CRYSTALS.get()) || ring.is(TheabyssModItems.RING_OF_TELEKINETIC.get())
            || net.yezon.theabyss.ring.secondary.Secondary.has(ring);
    }

    private static void secondary(ServerPlayer player, ItemStack ring) {
        if (ring.is(TheabyssModItems.RING_OF_CRYSTALS.get())) {
            ApplyRingOfCrystalsEvents.needles(player, ring);
        } else if (ring.is(TheabyssModItems.RING_OF_TELEKINETIC.get())) {
            ComeHere.cast(player, ring);
        } else {
            net.yezon.theabyss.ring.secondary.Secondary.cast(player, ring);
        }
    }
}
