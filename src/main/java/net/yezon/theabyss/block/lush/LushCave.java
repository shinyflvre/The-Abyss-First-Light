package net.yezon.theabyss.block.lush;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.yezon.theabyss.TheabyssMod;

public final class LushCave {
    public static final ResourceKey<Feature> MOSS_PATCH_BONEMEAL = feature("lush_moss_patch_bonemeal");
    public static final ResourceKey<Feature> AZALEA_TREE = feature("lush_azalea_tree");
    public static final TreeGrower AZALEA = new TreeGrower("theabyss_lush_azalea", WeightedList.of(AZALEA_TREE), WeightedList.of(), WeightedList.of(), AZALEA_TREE);

    private LushCave() {
    }

    private static ResourceKey<Feature> feature(String name) {
        return ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, name));
    }
}
