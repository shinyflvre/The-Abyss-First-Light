package net.yezon.theabyss.ring.fx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;

public final class RingAccent {
    public static final int FIRE = 1;
    public static final int FIRESTORM = 2;
    public static final int FIRE_TRAIL = 3;
    public static final int FIRE_HIT = 4;
    public static final int BLACK_TRAIL = 5;
    public static final int BLACK_HIT = 6;
    public static final int THUNDER = 7;
    public static final int ELECTRO = 8;
    public static final int FREEZE = 9;
    public static final int OCEAN = 10;
    public static final int REGENERATION = 11;
    public static final int CURSE = 12;
    public static final int STEP_DEPART = 14;
    public static final int STEP_ARRIVE = 15;
    public static final int SPEED = 16;
    public static final int JUGGER = 17;
    public static final int EAGLE = 18;
    public static final int TELEKINETIC = 19;
    public static final int NIGHTBLADE = 20;
    public static final int NATURE = 21;
    public static final int FANGS = 22;
    public static final int TIME = 24;
    public static final int HOME = 25;
    public static final int POCKET = 26;
    public static final int SEEKER = 28;
    public static final int SPECTRAL = 29;
    public static final int INVISIBILITY = 31;
    public static final int FLIGHT = 32;
    public static final int SLIDE = 33;
    public static final int ENDER = 34;
    public static final int METEOR = 40;
    public static final int VOID = 41;
    public static final int TRAIL = 42;
    public static final int SWAP = 43;
    public static final int REWIND = 44;
    public static final int TETHER = 45;
    public static final int TIDAL = 46;
    public static final int HEAL = 47;
    public static final int WALL = 48;
    public static final int HEX = 49;
    public static final int HEX_END = 50;
    public static final int OVERCHARGE = 52;
    public static final int OVERCHARGE_FIRE = 53;
    public static final int DASH = 54;
    public static final int FLAME_TRAIL = 55;
    public static final int ICE = 56;
    public static final int ICE_BURST = 57;
    public static final int RECALL_MARK = 58;
    public static final int RECALL = 59;
    public static final int SOUL = 60;
    public static final int ECHO_SUMMON = 61;
    public static final int ECHO_DISSOLVE = 62;

    private static final int MAX_TARGETS = 24;

    private RingAccent() {
    }

    public static void send(ServerLevel level, int code, Vec3 at) {
        SpellFxMessage.around(level, at, SpellFxMessage.ACCENT, List.of(), SpellFxMessage.points(at), code);
    }

    public static void send(ServerLevel level, int code, Vec3 at, Entity caster) {
        SpellFxMessage.around(level, at, SpellFxMessage.ACCENT, caster == null ? List.of() : List.of(caster.getId()), SpellFxMessage.points(at), code);
    }

    public static void send(ServerLevel level, int code, Vec3 at, Entity caster, Collection<? extends Entity> targets) {
        List<Integer> ids = new ArrayList<>();
        ids.add(caster == null ? -1 : caster.getId());
        for (Entity target : targets) {
            if (ids.size() > MAX_TARGETS) {
                break;
            }
            ids.add(target.getId());
        }
        SpellFxMessage.around(level, at, SpellFxMessage.ACCENT, ids, SpellFxMessage.points(at), code);
    }
}
