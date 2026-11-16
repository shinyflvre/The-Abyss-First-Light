package net.yezon.theabyss.fabric.server;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.TheabyssMod;

public final class FabricAttributes {
    public static final Holder<Attribute> SWIM_SPEED = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE,
        Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "swim_speed"), new RangedAttribute("theabyss.swim_speed", 1.0, 0.0, 1024.0).setSyncable(true));
    public static final Holder<Attribute> CREATIVE_FLIGHT = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE,
        Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "creative_flight"), new RangedAttribute("theabyss.creative_flight", 0.0, 0.0, 1.0).setSyncable(true));

    private FabricAttributes() {
    }

    public static void bootstrap() {
    }

    public static double swimSpeed(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(SWIM_SPEED);
        return instance == null ? 1.0 : instance.getValue();
    }

    public static boolean attributeFlight(Player player) {
        AttributeInstance instance = player.getAttribute(CREATIVE_FLIGHT);
        return instance != null && instance.getValue() > 0.0;
    }
}
