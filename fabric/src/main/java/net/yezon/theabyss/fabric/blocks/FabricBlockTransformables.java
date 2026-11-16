package net.yezon.theabyss.fabric.blocks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import net.fabricmc.fabric.api.item.v1.BlockTransformerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.yezon.theabyss.TheabyssMod;

public final class FabricBlockTransformables {
    private static final String FILE = "data/neoforge/data_maps/block/transformables.json";

    private FabricBlockTransformables() {
    }

    public static void register() {
        Path path = FabricLoader.getInstance().getModContainer(TheabyssMod.MODID).flatMap(container -> container.findPath(FILE))
            .orElseThrow(() -> new IllegalStateException("Missing " + FILE));
        JsonObject root;
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            root = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + FILE, e);
        }
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        for (Map.Entry<String, JsonElement> value : root.getAsJsonObject("values").entrySet()) {
            JsonObject entry = value.getValue().getAsJsonObject();
            if (entry.has("transformers")) {
                for (Map.Entry<String, JsonElement> transformer : entry.getAsJsonObject("transformers").entrySet()) {
                    add(value.getKey(), transformer.getKey(), transformer.getValue(), ops);
                }
            } else {
                add(value.getKey(), entry.get("transformer").getAsString(), entry.get("transform_data"), ops);
            }
        }
    }

    private static void add(String block, String transformer, JsonElement json, RegistryOps<JsonElement> ops) {
        BlockTransformer.BlockTransformData data = BlockTransformer.BlockTransformData.CODEC.parse(ops, json)
            .getOrThrow(message -> new IllegalStateException("Invalid transform of " + block + ": " + message));
        switch (transformer) {
            case "minecraft:axe" -> BlockTransformerHelper.registerAxe(data);
            case "minecraft:hoe" -> BlockTransformerHelper.registerHoe(data);
            case "minecraft:shovel" -> BlockTransformerHelper.registerShovel(data);
            default -> throw new IllegalStateException("Unknown block transformer " + transformer + " for " + block);
        }
    }
}
