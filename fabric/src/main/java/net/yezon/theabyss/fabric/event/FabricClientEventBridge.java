package net.yezon.theabyss.fabric.event;

public final class FabricClientEventBridge {
    private FabricClientEventBridge() {
    }

    public static void register() {
        ServerFabricEvents.registerClient();
        EntityFabricEvents.registerClient();
        ClientFabricEvents.register();
    }
}
