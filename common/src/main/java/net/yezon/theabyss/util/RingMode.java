package net.yezon.theabyss.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.network.TheabyssModVariables;

public final class RingMode {
    private RingMode() {
    }

    public static boolean spares(Entity caster, Entity target) {
        return target instanceof Player && caster instanceof Player player && TheabyssModVariables.getPlayerVariables(player).FriendMode;
    }

    public static boolean summon(Entity target) {
        return target instanceof net.yezon.theabyss.entity.SummonedSeekerEntity || target instanceof net.yezon.theabyss.entity.SummonedHollowSeekerEntity;
    }
}
