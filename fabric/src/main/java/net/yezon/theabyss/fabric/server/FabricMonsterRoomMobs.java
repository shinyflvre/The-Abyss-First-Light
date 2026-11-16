package net.yezon.theabyss.fabric.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.entity.EntityType;
import net.yezon.theabyss.TheabyssMod;
import org.jspecify.annotations.Nullable;

public final class FabricMonsterRoomMobs {
    private static final String FILE = "data/neoforge/data_maps/entity_type/monster_room_mobs.json";
    private static final int VANILLA_WEIGHT = 400;
    private static final Map<Identifier, Integer> WEIGHTS = new LinkedHashMap<>();
    private static volatile @Nullable List<Weighted<EntityType<?>>> resolved;
    private static int total;

    private FabricMonsterRoomMobs() {
    }

    public static void load() {
        WEIGHTS.clear();
        resolved = null;
        FabricLoader.getInstance().getModContainer(TheabyssMod.MODID).flatMap(mod -> mod.findPath(FILE)).ifPresent(FabricMonsterRoomMobs::read);
    }

    private static void read(Path file) {
        JsonObject json;
        try {
            json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Cannot read " + file, e);
        }
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("values").entrySet()) {
            WEIGHTS.put(Identifier.parse(entry.getKey()), entry.getValue().getAsJsonObject().get("weight").getAsInt());
        }
    }

    public static @Nullable EntityType<?> pick(RandomSource random) {
        List<Weighted<EntityType<?>>> mobs = resolved;
        if (mobs == null) {
            List<Weighted<EntityType<?>>> list = new ArrayList<>();
            int sum = 0;
            for (Map.Entry<Identifier, Integer> entry : WEIGHTS.entrySet()) {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(entry.getKey())
                    .orElseThrow(() -> new IllegalStateException("Nonexistent entity " + entry.getKey() + " in monster room mobs"));
                list.add(new Weighted<>(type, entry.getValue()));
                sum += entry.getValue();
            }
            total = sum;
            mobs = list;
            resolved = list;
        }
        if (mobs.isEmpty()) {
            return null;
        }
        int roll = random.nextInt(VANILLA_WEIGHT + total);
        for (Weighted<EntityType<?>> mob : mobs) {
            roll -= mob.weight();
            if (roll < 0) {
                return mob.value();
            }
        }
        return null;
    }
}
