package net.yezon.theabyss.client.debug;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.lang.reflect.Field;
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
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.yezon.theabyss.platform.common.world.ModifiableBiomeInfo;
import net.yezon.theabyss.TheabyssMod;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class BiomeColors {
    public enum Channel {
        FOG("Fog", "minecraft:visual/fog_color", EnvironmentAttributes.FOG_COLOR, false),
        SKY("Sky", "minecraft:visual/sky_color", EnvironmentAttributes.SKY_COLOR, false),
        WATER_FOG("Water Fog", "minecraft:visual/water_fog_color", EnvironmentAttributes.WATER_FOG_COLOR, false),
        WATER("Water", "effects.water_color", null, true),
        GRASS("Grass", "effects.grass_color", null, true),
        FOLIAGE("Foliage", "effects.foliage_color", null, true);

        public final String label;
        public final String jsonKey;
        final EnvironmentAttribute<Vector3fc> attribute;
        public final boolean baked;

        Channel(String label, String jsonKey, EnvironmentAttribute<Vector3fc> attribute, boolean baked) {
            this.label = label;
            this.jsonKey = jsonKey;
            this.attribute = attribute;
            this.baked = baked;
        }
    }

    private static final Path FILE = net.yezon.theabyss.platform.Platform.get().configDir().resolve("theabyss-biome-colors.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<Identifier, Map<Channel, Integer>> EDITS = new TreeMap<>();
    private static final Map<Biome, ModifiableBiomeInfo.BiomeInfo> ORIGINALS = new java.util.IdentityHashMap<>();
    private static boolean loaded;

    private BiomeColors() {
    }

    public static Holder<Biome> biomeAtCamera(Minecraft mc) {
        return mc.level.getBiome(mc.gameRenderer.mainCamera().blockPosition());
    }

    public static Identifier id(Holder<Biome> biome) {
        return biome.unwrapKey().map(ResourceKey::identifier).orElse(Identifier.withDefaultNamespace("unknown"));
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
            case WATER -> effects.waterColor() & 0xFFFFFF;
            case GRASS -> effects.grassColorOverride().orElseGet(() -> biome.getGrassColor(0.0, 0.0)) & 0xFFFFFF;
            case FOLIAGE -> effects.foliageColorOverride().orElseGet(biome::getFoliageColor) & 0xFFFFFF;
            default -> {
                Vector3fc dimension = mc.level.dimensionType().attributes().applyModifier(channel.attribute, channel.attribute.defaultValue());
                yield toRgb(info.attributes().applyModifier(channel.attribute, dimension));
            }
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
        int water = edits.containsKey(Channel.WATER) ? edits.get(Channel.WATER) : effects.waterColor();
        Optional<Integer> grass = edits.containsKey(Channel.GRASS) ? Optional.of(edits.get(Channel.GRASS)) : effects.grassColorOverride();
        Optional<Integer> foliage = edits.containsKey(Channel.FOLIAGE) ? Optional.of(edits.get(Channel.FOLIAGE)) : effects.foliageColorOverride();
        BiomeSpecialEffects newEffects = new BiomeSpecialEffects(water, foliage, effects.dryFoliageColorOverride(), grass, effects.grassColorModifier());

        EnvironmentAttributeMap.Builder attributes = EnvironmentAttributeMap.builder().putAll(original.attributes());
        for (Channel channel : new Channel[] {Channel.FOG, Channel.SKY, Channel.WATER_FOG}) {
            if (edits.containsKey(channel)) {
                attributes.set(channel.attribute, toVector(edits.get(channel)));
            }
        }
        EnvironmentAttributeMap newAttributes = attributes.build();
        ModifiableBiomeInfo.BiomeInfo modified = new ModifiableBiomeInfo.BiomeInfo(original.climateSettings(), newAttributes, newEffects, original.generationSettings());
        try {
            ModifiableBiomeInfo.setModified(biome, modified);
            setField(Biome.class, biome, "specialEffects", newEffects);
            setField(Biome.class, biome, "attributes", newAttributes);
        } catch (ReflectiveOperationException | RuntimeException e) {
            TheabyssMod.LOGGER.warn("[BiomeColors] could not change the biome colours", e);
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            mc.level.clearTintCaches();
        }
    }

    private static void setField(Class<?> owner, Object target, String name, Object value) throws ReflectiveOperationException {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
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
                    EDITS.put(Identifier.parse(biomeId), edits);
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

    private static int toRgb(Vector3fc colour) {
        int r = Math.round(Math.clamp(colour.x(), 0.0F, 1.0F) * 255.0F);
        int g = Math.round(Math.clamp(colour.y(), 0.0F, 1.0F) * 255.0F);
        int b = Math.round(Math.clamp(colour.z(), 0.0F, 1.0F) * 255.0F);
        return r << 16 | g << 8 | b;
    }

    private static Vector3f toVector(int rgb) {
        return new Vector3f((rgb >> 16 & 255) / 255.0F, (rgb >> 8 & 255) / 255.0F, (rgb & 255) / 255.0F);
    }
}
