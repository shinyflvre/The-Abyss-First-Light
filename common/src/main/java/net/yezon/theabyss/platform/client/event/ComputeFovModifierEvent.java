package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public class ComputeFovModifierEvent extends Event {
    private float newFovModifier;
    private final net.minecraft.world.entity.player.Player player;

    public ComputeFovModifierEvent(float newFovModifier, net.minecraft.world.entity.player.Player player) {
        this.newFovModifier = newFovModifier;
        this.player = player;
    }

    public float getNewFovModifier() {
        return this.newFovModifier;
    }

    public void setNewFovModifier(float value) {
        this.newFovModifier = value;
    }

    public net.minecraft.world.entity.player.Player getPlayer() {
        return this.player;
    }
}
