package net.yezon.theabyss.fabric.event;

public final class FabricEventBridge {
    private FabricEventBridge() {
    }

    public static void beforeRegistration() {
    }

    public static void register() {
        ServerFabricEvents.register();
        EntityFabricEvents.register();
    }
}
