package net.yezon.theabyss.client.debug;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.yezon.theabyss.platform.common.world.ModifiableBiomeInfo;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.mixin.client.BiomeEffectsAccessor;

public final class BiomeColors {
    public enum Channel {
        FOG("Fog", "minecraft:visual/fog_color", false),
        SKY("Sky", "minecraft:visual/sky_color", false),
        WATER_FOG("Water Fog", "minecraft:visual/water_fog_color", false),
        WATER("Water", "effects.water_color", true),
        GRASS("Grass", "effects.grass_color", true),
        FOLIAGE("Foliage", "effects.foliage_color", true);

        public final String label;
        public final String jsonKey;
        public final boolean baked;

        Channel(String label, String jsonKey, boolean baked) {
            this.label = label;
            this.jsonKey = jsonKey;
            this.baked = baked;
        }
    }

    private static final Path FILE = net.yezon.theabyss.platform.Platform.get().configDir().resolve("theabyss-biome-colors.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<ResourceLocation, Map<Channel, Integer>> EDITS = new TreeMap<>();
    private static final Map<Biome, ModifiableBiomeInfo.BiomeInfo> ORIGINALS = new java.util.IdentityHashMap<>();
    private static boolean loaded;

    private BiomeColors() {
    }

    public static Holder<Biome> biomeAtCamera(Minecraft mc) {
        return mc.level.getBiome(mc.gameRenderer.getMainCamera().getBlockPosition());
    }

    public static ResourceLocation id(Holder<Biome> biome) {
        return biome.unwrapKey().map(ResourceKey::location).orElse(ResourceLocation.withDefaultNamespace("unknown"));
    }

    public static int current(Minecraft mc, Holder<Biome> holder, Channel channel) {
        return read(mc, holder.value(), info(holder.value()), channel);
    }

    public static int original(Minecraft mc, Holder<Biome> holder, Channel channel) {
        Biome biome = holder.value();
        ModifiableBiomeInfo.BiomeInfo original = ORIGINALS.get(biome);
        return read(mc, biome, original != null ? original : info(biome), channel);
    }

    public static boolean isEdited(Holder<Biome> holder, Channel channel) {
        Map<Channel, Integer> edits = EDITS.get(id(holder));
        return edits != null && edits.containsKey(channel);
    }

    private static int read(Minecraft mc, Biome biome, ModifiableBiomeInfo.BiomeInfo info, Channel channel) {
        BiomeSpecialEffects effects = info.effects();
        return switch (channel) {
            case FOG -> effects.getFogColor() & 0xFFFFFF;
            case SKY -> effects.getSkyColor() & 0xFFFFFF;
            case WATER_FOG -> effects.getWaterFogColor() & 0xFFFFFF;
            case WATER -> effects.getWaterColor() & 0xFFFFFF;
            case GRASS -> effects.getGrassColorOverride().orElseGet(() -> biome.getGrassColor(0.0, 0.0)) & 0xFFFFFF;
            case FOLIAGE -> effects.getFoliageColorOverride().orElseGet(biome::getFoliageColor) & 0xFFFFFF;
        };
    }

    private static ModifiableBiomeInfo.BiomeInfo info(Biome biome) {
        return ModifiableBiomeInfo.get(biome);
    }

    public static boolean set(Holder<Biome> holder, Channel channel, int rgb) {
        ensureLoaded();
        EDITS.computeIfAbsent(id(holder), k -> new EnumMap<>(Channel.class)).put(channel, rgb & 0xFFFFFF);
        apply(holder.value(), EDITS.get(id(holder)));
        save();
        return channel.baked;
    }

    public static void reset(Holder<Biome> holder, Channel channel) {
        Map<Channel, Integer> edits = EDITS.get(id(holder));
        if (edits != null && edits.remove(channel) != null) {
            apply(holder.value(), edits);
            if (edits.isEmpty()) {
                EDITS.remove(id(holder));
            }
            save();
        }
    }

    public static void resetBiome(Holder<Biome> holder) {
        if (EDITS.remove(id(holder)) != null) {
            apply(holder.value(), Map.of());
            save();
        }
    }

    public static void applySaved(Minecraft mc) {
        ensureLoaded();
        if (mc.level == null) {
            return;
        }
        var registry = mc.level.registryAccess().lookupOrThrow(Registries.BIOME);
        EDITS.forEach((id, edits) -> registry.get(ResourceKey.create(Registries.BIOME, id)).ifPresent(holder -> apply(holder.value(), edits)));
    }

    private static void apply(Biome biome, Map<Channel, Integer> edits) {
        ModifiableBiomeInfo.BiomeInfo original = ORIGINALS.computeIfAbsent(biome, BiomeColors::info);
        BiomeSpecialEffects effects = original.effects();
        int water = edits.containsKey(Channel.WATER) ? edits.get(Channel.WATER) : effects.getWaterColor();
        Optional<Integer> grass = edits.containsKey(Channel.GRASS) ? Optional.of(edits.get(Channel.GRASS)) : effects.getGrassColorOverride();
        Optional<Integer> foliage = edits.containsKey(Channel.FOLIAGE) ? Optional.of(edits.get(Channel.FOLIAGE)) : effects.getFoliageColorOverride();
        BiomeSpecialEffects.Builder builder = new BiomeSpecialEffects.Builder()
            .fogColor(edits.containsKey(Channel.FOG) ? edits.get(Channel.FOG) : effects.getFogColor())
            .skyColor(edits.containsKey(Channel.SKY) ? edits.get(Channel.SKY) : effects.getSkyColor())
            .waterFogColor(edits.containsKey(Channel.WATER_FOG) ? edits.get(Channel.WATER_FOG) : effects.getWaterFogColor())
            .waterColor(water)
            .grassColorModifier(effects.getGrassColorModifier());
        grass.ifPresent(builder::grassColorOverride);
        foliage.ifPresent(builder::foliageColorOverride);
        effects.getAmbientParticleSettings().ifPresent(builder::ambientParticle);
        effects.getAmbientLoopSoundEvent().ifPresent(builder::ambientLoopSound);
        effects.getAmbientMoodSettings().ifPresent(builder::ambientMoodSound);
        effects.getAmbientAdditionsSettings().ifPresent(builder::ambientAdditionsSound);
        effects.getBackgroundMusic().ifPresent(builder::backgroundMusic);
        BiomeSpecialEffects newEffects = builder.build();
        ModifiableBiomeInfo.BiomeInfo modified = new ModifiableBiomeInfo.BiomeInfo(original.climateSettings(), newEffects, original.generationSettings(),
            original.mobSpawnSettings());
        try {
            ModifiableBiomeInfo.setModified(biome, modified);
            ((BiomeEffectsAccessor)(Object)biome).theabyss$setSpecialEffects(newEffects);
        } catch (RuntimeException e) {
            TheabyssMod.LOGGER.warn("[BiomeColors] could not change the biome colours", e);
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            mc.level.clearTintCaches();
        }
    }

    public static String export() {
        ensureLoaded();
        JsonObject all = new JsonObject();
        EDITS.forEach((id, edits) -> {
            JsonObject biome = new JsonObject();
            edits.forEach((channel, rgb) -> biome.addProperty(channel.jsonKey, hex(rgb)));
            all.add(id.toString(), biome);
        });
        return GSON.toJson(all);
    }

    public static int editedBiomes() {
        ensureLoaded();
        return EDITS.size();
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        if (!Files.exists(FILE)) {
            return;
        }
        try {
            JsonObject all = GSON.fromJson(Files.readString(FILE), JsonObject.class);
            for (String biomeId : all.keySet()) {
                JsonObject biome = all.getAsJsonObject(biomeId);
                Map<Channel, Integer> edits = new EnumMap<>(Channel.class);
                for (Channel channel : Channel.values()) {
                    if (biome.has(channel.jsonKey)) {
                        edits.put(channel, parseHex(biome.get(channel.jsonKey).getAsString()));
                    }
                }
                if (!edits.isEmpty()) {
                    EDITS.put(ResourceLocation.parse(biomeId), edits);
                }
            }
        } catch (IOException | RuntimeException e) {
            TheabyssMod.LOGGER.warn("[BiomeColors] could not read {}", FILE, e);
        }
    }

    private static void save() {
        try {
            Files.writeString(FILE, export());
        } catch (IOException e) {
            TheabyssMod.LOGGER.warn("[BiomeColors] could not write {}", FILE, e);
        }
    }

    public static String hex(int rgb) {
        return String.format(Locale.ROOT, "#%06x", rgb & 0xFFFFFF);
    }

    public static int parseHex(String text) {
        String s = text.trim();
        if (s.startsWith("#")) {
            s = s.substring(1);
        }
        if (s.length() != 6) {
            throw new NumberFormatException(text);
        }
        return Integer.parseInt(s, 16);
    }
}
