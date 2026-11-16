package net.yezon.theabyss.fabric.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.yezon.theabyss.TheabyssMod;

public final class FabricBiomeModifiers {
    private static final String DIRECTORY = "data/" + TheabyssMod.MODID + "/neoforge/biome_modifier";
    private static final Codec<WeightedList<MobSpawnSettings.SpawnerData>> SPAWNERS = Codec.either(
        WeightedList.codec(MobSpawnSettings.SpawnerData.CODEC), Weighted.codec(MobSpawnSettings.SpawnerData.CODEC)
    ).xmap(either -> either.map(Function.identity(), single -> WeightedList.of(List.of(single))), list -> Either.left(list));

    private FabricBiomeModifiers() {
    }

    public static void register() {
        ModContainer mod = FabricLoader.getInstance().getModContainer(TheabyssMod.MODID)
            .orElseThrow(() -> new IllegalStateException("The Abyss mod container is missing"));
        Map<String, Path> files = new TreeMap<>();
        for (Path root : mod.getRootPaths()) {
            Path directory = root.resolve(DIRECTORY);
            if (!Files.isDirectory(directory)) {
                continue;
            }
            try (Stream<Path> stream = Files.list(directory)) {
                stream.forEach(file -> {
                    String name = file.getFileName().toString();
                    if (name.endsWith(".json")) {
                        files.putIfAbsent(name.substring(0, name.length() - ".json".length()), file);
                    }
                });
            } catch (IOException e) {
                throw new IllegalStateException("Cannot list the biome modifiers in " + directory, e);
            }
        }
        files.forEach(FabricBiomeModifiers::register);
    }

    private static void register(String name, Path file) {
        JsonObject json;
        try {
            json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Cannot read the biome modifier " + file, e);
        }
        Identifier id = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, name);
        Predicate<BiomeSelectionContext> biomes = biomes(json.get("biomes"), id);
        String type = json.get("type").getAsString();
        switch (type) {
            case "neoforge:add_features" -> {
                GenerationStep.Decoration step = GenerationStep.Decoration.CODEC.parse(JsonOps.INSTANCE, json.get("step"))
                    .getOrThrow(error -> new IllegalStateException("Biome modifier " + id + ": " + error));
                List<ResourceKey<PlacedFeature>> features = new ArrayList<>();
                for (Identifier feature : ids(json.get("features"), id)) {
                    features.add(ResourceKey.create(Registries.PLACED_FEATURE, feature));
                }
                BiomeModifications.create(id).add(ModificationPhase.ADDITIONS, biomes, context -> {
                    for (ResourceKey<PlacedFeature> feature : features) {
                        context.getGenerationSettings().addFeature(step, feature);
                    }
                });
            }
            case "neoforge:add_spawns" -> {
                WeightedList<MobSpawnSettings.SpawnerData> spawners = SPAWNERS.parse(JsonOps.INSTANCE, json.get("spawners"))
                    .getOrThrow(error -> new IllegalStateException("Biome modifier " + id + ": " + error));
                BiomeModifications.create(id).add(ModificationPhase.ADDITIONS, biomes, context -> {
                    for (Weighted<MobSpawnSettings.SpawnerData> spawner : spawners.unwrap()) {
                        context.getMobSpawnSettings().addSpawn(spawner.value().type(), spawner.weight(), spawner.value().count());
                    }
                });
            }
            default -> throw new IllegalStateException("Unknown biome modifier type " + type + " in " + id);
        }
    }

    private static Predicate<BiomeSelectionContext> biomes(JsonElement element, Identifier modifier) {
        if (element != null && element.isJsonPrimitive() && element.getAsString().startsWith("#")) {
            return BiomeSelectors.tag(TagKey.create(Registries.BIOME, Identifier.parse(element.getAsString().substring(1))));
        }
        List<ResourceKey<Biome>> keys = new ArrayList<>();
        for (Identifier biome : ids(element, modifier)) {
            keys.add(ResourceKey.create(Registries.BIOME, biome));
        }
        return BiomeSelectors.includeByKey(keys);
    }

    private static List<Identifier> ids(JsonElement element, Identifier modifier) {
        List<Identifier> ids = new ArrayList<>();
        if (element != null && element.isJsonPrimitive() && !element.getAsString().startsWith("#")) {
            ids.add(Identifier.parse(element.getAsString()));
        } else if (element != null && element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement entry : array) {
                ids.add(Identifier.parse(entry.getAsString()));
            }
        } else {
            throw new IllegalStateException("Unsupported holder set " + element + " in biome modifier " + modifier);
        }
        return ids;
    }
}
