package net.yezon.theabyss.fabric.event;

public final class FabricDamageFrame {
    final float original;
    float blocked;
    float inflicted;
    boolean reachedEnd;

    FabricDamageFrame(float original) {
        this.original = original;
    }
}
