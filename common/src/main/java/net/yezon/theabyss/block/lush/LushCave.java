package net.yezon.theabyss.block.lush;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.yezon.theabyss.TheabyssMod;

public final class LushCave {
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOSS_PATCH_BONEMEAL = feature("lush_moss_patch_bonemeal");
    public static final ResourceKey<ConfiguredFeature<?, ?>> AZALEA_TREE = feature("lush_azalea_tree");
    public static final TreeGrower AZALEA = new TreeGrower("theabyss_lush_azalea", Optional.empty(), Optional.of(AZALEA_TREE), Optional.empty());

    private LushCave() {
    }

    public static OptionalInt azaleaMinimumHeight(ServerLevel level) {
        return level.registryAccess()
            .registryOrThrow(Registries.CONFIGURED_FEATURE)
            .getHolder(AZALEA_TREE)
            .map(holder -> holder.value().config() instanceof TreeConfiguration tree ? baseHeight(tree.trunkPlacer) : OptionalInt.empty())
            .orElse(OptionalInt.empty());
    }

    private static OptionalInt baseHeight(TrunkPlacer placer) {
        return TrunkPlacer.CODEC.encodeStart(JsonOps.INSTANCE, placer).result()
            .filter(JsonElement::isJsonObject)
            .map(json -> json.getAsJsonObject().get("base_height"))
            .filter(element -> element != null && element.isJsonPrimitive())
            .map(element -> OptionalInt.of(element.getAsInt()))
            .orElse(OptionalInt.empty());
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> feature(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, name));
    }
}
