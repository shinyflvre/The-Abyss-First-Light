package net.yezon.theabyss.platform.common;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.yezon.theabyss.platform.services.ServerHooks;

public final class NeoForgeMod {
    public static final Holder<Attribute> SWIM_SPEED = ServerHooks.get().swimSpeedAttribute();
    public static final Holder<Attribute> CREATIVE_FLIGHT = ServerHooks.get().creativeFlightAttribute();

    private NeoForgeMod() {
    }
}
