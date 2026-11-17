package net.yezon.theabyss.ring;

import java.util.function.UnaryOperator;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModItems;
import org.jspecify.annotations.Nullable;

public final class Gauntlet {
    public static final int SLOTS = 5;
    private static final String SELECTED_KEY = "theabyss_gauntlet_selected";

    private Gauntlet() {
    }

    public static boolean is(ItemStack stack) {
        return stack.is(TheabyssModItems.INFECTED_GAUNTLET.get());
    }

    public static boolean isRing(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getNamespace().equals(TheabyssMod.MODID) && id.getPath().startsWith("ring_of_") && !id.getPath().equals("ring_of_ring")
            && !id.getPath().endsWith("_attack");
    }

    public static @Nullable InteractionHand hand(Player player) {
        if (is(player.getMainHandItem())) {
            return InteractionHand.MAIN_HAND;
        }
        return is(player.getOffhandItem()) ? InteractionHand.OFF_HAND : null;
    }

    public static ItemStack held(Player player) {
        InteractionHand hand = hand(player);
        return hand == null ? ItemStack.EMPTY : player.getItemInHand(hand);
    }

    public static NonNullList<ItemStack> rings(ItemStack gauntlet) {
        NonNullList<ItemStack> out = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        gauntlet.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(out);
        return out;
    }

    public static void setRings(ItemStack gauntlet, NonNullList<ItemStack> rings) {
        gauntlet.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(rings));
    }

    public static int selected(ItemStack gauntlet) {
        CustomData data = gauntlet.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        int slot = net.yezon.theabyss.compat.NbtCompat.getIntOr(data.copyTag(), SELECTED_KEY, 0);
        return slot < 0 || slot >= SLOTS ? 0 : slot;
    }

    public static void select(ItemStack gauntlet, int slot) {
        CustomData.update(DataComponents.CUSTOM_DATA, gauntlet, tag -> tag.putInt(SELECTED_KEY, Math.max(0, Math.min(SLOTS - 1, slot))));
    }

    public static ItemStack selectedRing(ItemStack gauntlet) {
        return rings(gauntlet).get(selected(gauntlet));
    }

    public static void withSelectedRing(ItemStack gauntlet, UnaryOperator<ItemStack> use) {
        NonNullList<ItemStack> rings = rings(gauntlet);
        int slot = selected(gauntlet);
        ItemStack ring = rings.get(slot);
        if (ring.isEmpty()) {
            return;
        }
        ItemStack after = use.apply(ring);
        rings.set(slot, after == null || after.isEmpty() ? ItemStack.EMPTY : after);
        setRings(gauntlet, rings);
    }
}
