package net.yezon.theabyss.platform.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterCommandsEvent extends Event {
    public abstract com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> getDispatcher();
}
