package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public class AddSectionGeometryEvent extends Event {
    private final net.minecraft.world.level.Level level;
    private final net.minecraft.core.BlockPos sectionOrigin;

    public AddSectionGeometryEvent(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos sectionOrigin) {
        this.level = level;
        this.sectionOrigin = sectionOrigin;
    }

    public net.minecraft.world.level.Level getLevel() {
        return this.level;
    }

    public net.minecraft.core.BlockPos getSectionOrigin() {
        return this.sectionOrigin;
    }
}
