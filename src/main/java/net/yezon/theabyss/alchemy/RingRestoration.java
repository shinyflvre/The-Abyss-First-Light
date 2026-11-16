package net.yezon.theabyss.alchemy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.ring.Gauntlet;

public final class RingRestoration {
    public static final int POINTS = 100;

    private RingRestoration() {
    }

    public record Result(int restored, int rings) {
    }

    private static boolean worn(ItemStack stack) {
        return Gauntlet.isRing(stack) && stack.isDamageableItem() && stack.getDamageValue() > 0;
    }

    public static Result restore(Player player, int points) {
        List<ItemStack> rings = new ArrayList<>();
        Map<ItemStack, NonNullList<ItemStack>> gauntlets = new IdentityHashMap<>();
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (Gauntlet.is(stack)) {
                NonNullList<ItemStack> inside = Gauntlet.rings(stack);
                gauntlets.put(stack, inside);
                for (ItemStack ring : inside) {
                    if (worn(ring)) {
                        rings.add(ring);
                    }
                }
            } else if (worn(stack)) {
                rings.add(stack);
            }
        }
        Set<ItemStack> touched = Collections.newSetFromMap(new IdentityHashMap<>());
        int left = points;
        while (left > 0) {
            List<ItemStack> open = rings.stream().filter(RingRestoration::worn).toList();
            if (open.isEmpty()) {
                break;
            }
            int share = Math.max(1, left / open.size());
            for (ItemStack ring : open) {
                if (left <= 0) {
                    break;
                }
                int fix = Math.min(Math.min(share, ring.getDamageValue()), left);
                ring.setDamageValue(ring.getDamageValue() - fix);
                left -= fix;
                touched.add(ring);
            }
        }
        gauntlets.forEach(Gauntlet::setRings);
        return new Result(points - left, touched.size());
    }
}
