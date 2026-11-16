package net.yezon.theabyss.skill;

import java.util.List;
import org.jspecify.annotations.Nullable;

public enum Skill {
    REFLECTION("reflection", -1, 0, 0),
    CLEAR_EYES("clear_eyes", 0, 1, 5, REFLECTION),
    HUNGRY_MIND("hungry_mind", 0, 2, 10, CLEAR_EYES),
    CALM_MIND("calm_mind", 0, 3, 10, CLEAR_EYES),
    IRON_WILL("iron_will", 0, 4, 15, HUNGRY_MIND, CALM_MIND),
    FORESIGHT("foresight", 0, 5, 30, IRON_WILL),
    LIGHT_FEET("light_feet", 1, 1, 5, REFLECTION),
    SHADOW_DASH("shadow_dash", 1, 2, 10, LIGHT_FEET),
    BLEEDING_EDGE("bleeding_edge", 1, 3, 10, LIGHT_FEET),
    FEAST("feast", 1, 4, 15, SHADOW_DASH, BLEEDING_EDGE),
    WRAITH_FORM("wraith_form", 1, 5, 30, FEAST),
    HARDENED_SKIN("hardened_skin", 2, 1, 5, REFLECTION),
    DEEP_LUNGS("deep_lungs", 2, 2, 10, HARDENED_SKIN),
    ROOT_GRIP("root_grip", 2, 3, 10, HARDENED_SKIN),
    UNBROKEN("unbroken", 2, 4, 15, DEEP_LUNGS, ROOT_GRIP),
    ABYSS_HEART("abyss_heart", 2, 5, 30, UNBROKEN);

    public static final String[] BRANCHES = {"senses", "shadow", "body"};

    private final String id;
    private final int branch;
    private final int slot;
    private final int cost;
    private final List<Skill> parents;

    Skill(String id, int branch, int slot, int cost, Skill... parents) {
        this.id = id;
        this.branch = branch;
        this.slot = slot;
        this.cost = cost;
        this.parents = List.of(parents);
    }

    public String id() {
        return this.id;
    }

    public int branch() {
        return this.branch;
    }

    public int slot() {
        return this.slot;
    }

    public int cost() {
        return this.cost;
    }

    public List<Skill> parents() {
        return this.parents;
    }

    public boolean keystone() {
        return this.slot == 5;
    }

    public static @Nullable Skill byId(String id) {
        for (Skill skill : values()) {
            if (skill.id.equals(id)) {
                return skill;
            }
        }
        return null;
    }
}
