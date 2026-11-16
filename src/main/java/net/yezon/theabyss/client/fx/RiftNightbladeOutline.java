package net.yezon.theabyss.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

public final class RiftNightbladeOutline {
    private static final double RANGE = 16.0;
    private static final double BELOW = 3.0;

    private RiftNightbladeOutline() {
    }

    public static boolean visible(Entity figure) {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && player.level() == figure.level() && player.getY() > figure.getY() - BELOW
            && player.distanceToSqr(figure) < RANGE * RANGE;
    }
}
