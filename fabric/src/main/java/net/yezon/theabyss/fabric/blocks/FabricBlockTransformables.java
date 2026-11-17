package net.yezon.theabyss.fabric.blocks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.yezon.theabyss.TheabyssMod;

public final class FabricBlockTransformables {
    private static final String FILE = "data/neoforge/data_maps/block/strippables.json";

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
        for (Map.Entry<String, JsonElement> value : root.getAsJsonObject("values").entrySet()) {
            String stripped = value.getValue().getAsJsonObject().get("stripped_block").getAsString();
            StrippableBlockRegistry.register(block(value.getKey()), block(stripped));
        }
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(id))
            .orElseThrow(() -> new IllegalStateException("Unknown block " + id + " in " + FILE));
    }
}
