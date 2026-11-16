package net.yezon.theabyss.platform.event.entity.living;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public class LivingExperienceDropEvent extends Event implements ICancellableEvent {
    private final net.minecraft.world.entity.player.Player attackingPlayer;
    private int droppedExperience;
    private final net.minecraft.world.entity.LivingEntity entity;

    public LivingExperienceDropEvent(net.minecraft.world.entity.player.Player attackingPlayer, int droppedExperience, net.minecraft.world.entity.LivingEntity entity) {
        this.attackingPlayer = attackingPlayer;
        this.droppedExperience = droppedExperience;
        this.entity = entity;
    }

    public net.minecraft.world.entity.player.Player getAttackingPlayer() {
        return this.attackingPlayer;
    }

    public int getDroppedExperience() {
        return this.droppedExperience;
    }

    public void setDroppedExperience(int value) {
        this.droppedExperience = value;
    }

    public net.minecraft.world.entity.LivingEntity getEntity() {
        return this.entity;
    }
}
