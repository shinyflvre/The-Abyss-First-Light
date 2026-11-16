package net.yezon.theabyss.platform.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterTooltipAppendersEvent extends Event {
    public abstract void registerAppender(net.yezon.theabyss.platform.common.tooltip.TooltipLocation a0, net.yezon.theabyss.platform.common.tooltip.TooltipAppender a1);
}
