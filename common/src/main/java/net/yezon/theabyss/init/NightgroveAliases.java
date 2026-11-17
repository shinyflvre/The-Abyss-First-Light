package net.yezon.theabyss.init;

import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;

public final class NightgroveAliases {
    private static final String[] BLOCKS = {
        "log", "planks", "stairs", "slab", "trap_door", "door", "fence", "fence_gate", "pressure_plate", "button", "leaves", "melon", "moss", "dirt",
        "leave_carpet", "grass", "sign", "wall_sign", "hanging_sign", "wall_hanging_sign", "shelf"
    };

    private NightgroveAliases() {
    }

    public static void register() {
        for (String name : BLOCKS) {
            alias(TheabyssModBlocks.REGISTRY, "jungle_" + name, "nightgrove_" + name);
            alias(TheabyssModItems.REGISTRY, "jungle_" + name, "nightgrove_" + name);
        }
        for (String name : new String[] {"stripped_%s_log", "tall_%s_grass"}) {
            alias(TheabyssModBlocks.REGISTRY, String.format(name, "jungle"), String.format(name, "nightgrove"));
            alias(TheabyssModItems.REGISTRY, String.format(name, "jungle"), String.format(name, "nightgrove"));
        }
        for (String name : new String[] {"melon_item", "boat", "chest_boat"}) {
            alias(TheabyssModItems.REGISTRY, "jungle_" + name, "nightgrove_" + name);
        }
        alias(TheabyssModEntities.REGISTRY, "jungle_boat", "nightgrove_boat");
        alias(TheabyssModEntities.REGISTRY, "jungle_chest_boat", "nightgrove_chest_boat");
    }

    private static void alias(DeferredRegister<?> registry, String from, String to) {
        registry.addAlias(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, from), ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, to));
    }
}
