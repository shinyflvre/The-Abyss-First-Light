package net.yezon.theabyss.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

final class RangedItemHelper {
    private RangedItemHelper() {
    }

    static ItemStack findAmmo(Player player, Item ammo) {
        ItemStack stack = ProjectileWeaponItem.getHeldProjectile(player, candidate -> candidate.is(ammo));
        if (stack.isEmpty()) {
            for (ItemStack candidate : player.getInventory().getNonEquipmentItems()) {
                if (candidate.is(ammo)) {
                    return candidate;
                }
            }
        }
        return stack;
    }

    static void consumeAmmo(ServerPlayer player, ItemStack ammo) {
        if (ammo.isEmpty()) {
            return;
        }
        if (ammo.isDamageableItem()) {
            ammo.hurtAndBreak(1, player.level(), player, broken -> {
            });
        } else {
            ammo.shrink(1);
        }
        if (ammo.isEmpty()) {
            player.getInventory().removeItem(ammo);
        }
    }
}
