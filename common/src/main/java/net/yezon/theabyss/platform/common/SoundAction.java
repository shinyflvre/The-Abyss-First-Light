package net.yezon.theabyss.platform.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SoundAction {
    private static final Map<String, SoundAction> BY_NAME = new ConcurrentHashMap<>();

    private final String name;

    private SoundAction(String name) {
        this.name = name;
    }

    public static SoundAction get(String name) {
        return BY_NAME.computeIfAbsent(name, SoundAction::new);
    }

    public String name() {
        return this.name;
    }

    @Override
    public String toString() {
        return "SoundAction[" + this.name + "]";
    }
}
