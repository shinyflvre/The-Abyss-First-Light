package net.yezon.theabyss.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.yezon.theabyss.TheabyssMod;

public final class TheabyssModItemTags {
    public static final TagKey<Item> REPAIRS_ABERYTHE = repairs("aberythe");
    public static final TagKey<Item> REPAIRS_BONE = repairs("bone");
    public static final TagKey<Item> REPAIRS_FUSION = repairs("fusion");
    public static final TagKey<Item> REPAIRS_GARNITE = repairs("garnite");
    public static final TagKey<Item> REPAIRS_GLACERYTHE = repairs("glacerythe");
    public static final TagKey<Item> REPAIRS_IGNISITHE = repairs("ignisithe");
    public static final TagKey<Item> REPAIRS_INCORYTHE = repairs("incorythe");
    public static final TagKey<Item> REPAIRS_KNIGHT = repairs("knight");
    public static final TagKey<Item> REPAIRS_PHANTOM = repairs("phantom");
    public static final TagKey<Item> REPAIRS_PHANTOM_SWORD = repairs("phantom_sword");
    public static final TagKey<Item> REPAIRS_UNORITHE = repairs("unorithe");
    public static final TagKey<Item> REPAIRS_NONE = repairs("none");

    private TheabyssModItemTags() {
    }

    private static TagKey<Item> repairs(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "repairs/" + name));
    }
}
