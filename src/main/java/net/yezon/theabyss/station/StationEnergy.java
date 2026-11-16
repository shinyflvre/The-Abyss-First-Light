package net.yezon.theabyss.station;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.init.TheabyssModItems;

public final class StationEnergy {
    private StationEnergy() {
    }

    public static boolean isEnergy(ItemStack stack) {
        return stack.is(TheabyssModItems.LORAN_ENERGY.get());
    }

    public static boolean available(ItemStack energy) {
        return isEnergy(energy) && energy.getCount() == 1;
    }

    public static void pay(ServerLevel level, Container container, int slot) {
        ItemStack energy = container.getItem(slot);
        if (!available(energy)) {
            return;
        }
        energy.hurtAndBreak(1, level, (ServerPlayer)null, broken -> {
        });
        container.setItem(slot, energy.isEmpty() ? ItemStack.EMPTY : energy);
    }

    public static float left(ItemStack energy) {
        return isEnergy(energy) && energy.isDamageableItem() ? 1.0F - energy.getDamageValue() / (float)energy.getMaxDamage() : 0.0F;
    }

    public static int usesLeft(ItemStack energy) {
        return isEnergy(energy) && energy.isDamageableItem() ? energy.getMaxDamage() - energy.getDamageValue() : 0;
    }
}
