package net.yezon.theabyss.fabric.client;

public final class FabricHudHeights {
    private static final int VANILLA_LEFT = 39;
    private static int left = VANILLA_LEFT;
    private static int reservedRight;
    private static int reservedLastFrame;

    private FabricHudHeights() {
    }

    public static void beginFrame() {
        reservedLastFrame = reservedRight;
        reservedRight = 0;
    }

    public static void hearts(int guiHeight, int heartsY) {
        left = guiHeight - heartsY;
    }

    public static int airY(int guiHeight, int airY) {
        int reserved = Math.max(reservedRight, reservedLastFrame);
        return reserved > 0 ? Math.min(airY, guiHeight - reserved) : airY;
    }

    static int left() {
        return left;
    }

    static void reserveRight(int minHeight) {
        reservedRight = Math.max(reservedRight, minHeight);
    }
}
