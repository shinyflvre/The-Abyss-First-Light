package net.yezon.theabyss.ring.fx;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;

public final class SpellCircle {
    public static final int RUNES = 0;
    public static final int ORNATE = 1;
    public static final int TECH = 2;
    public static final int DISC = 3;
    public static final int RIPPLE = 4;
    public static final int SHOCK = 5;
    public static final int CRATER = 6;
    public static final int CRACK = 7;
    public static final int FROST = 8;
    public static final int SLASH = 9;
    public static final int ARCS = 10;
    public static final int HOVER = 11;
    public static final int CLOSING = 12;

    private SpellCircle() {
    }

    public static void at(ServerLevel level, int style, Vec3 feet, int colour, float radius, int life) {
        send(level, style, feet, List.of(), colour, radius, life);
    }

    public static void on(ServerLevel level, int style, Entity follow, int colour, float radius, int life) {
        send(level, style, follow.position(), List.of(follow.getId()), colour, radius, life);
    }

    private static void send(ServerLevel level, int style, Vec3 feet, List<Integer> ids, int colour, float radius, int life) {
        SpellFxMessage.around(level, feet, SpellFxMessage.CIRCLE, ids, List.of(feet.x, feet.y, feet.z, (double)colour, (double)radius, (double)life), style);
    }
}
