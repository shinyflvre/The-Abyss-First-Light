package net.yezon.theabyss.configuration;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ConfigConfiguration {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<Double> SOMNIUM_XPOS;
    public static final ModConfigSpec.ConfigValue<Double> SOMNIUM_YPOS;
    public static final ModConfigSpec.ConfigValue<Boolean> GUIDE_BOOK;
    public static final ModConfigSpec.ConfigValue<Boolean> PARTICLE_SET;
    public static final ModConfigSpec.ConfigValue<Boolean> SHADER_SET;
    public static final ModConfigSpec.ConfigValue<Double> OREGEN_SET;
    public static final ModConfigSpec.ConfigValue<Double> STRUCTURE_SET;
    public static final ModConfigSpec.ConfigValue<Boolean> ABYSS_NEBULA_SKY;
    public static final ModConfigSpec.ConfigValue<Boolean> VOLUMETRIC_FOG;
    public static final ModConfigSpec.ConfigValue<Boolean> AMBIENT_SMOKE;
    public static final ModConfigSpec.ConfigValue<Boolean> GROUND_FOG;
    public static final ModConfigSpec.ConfigValue<Boolean> ROOT_CRACKS;
    public static final ModConfigSpec.ConfigValue<Boolean> BOSS_HELP;
    public static final ModConfigSpec.ConfigValue<Boolean> SHADOW_VEIL;
    public static final ModConfigSpec.IntValue MOB_HEALTH;
    public static final ModConfigSpec.IntValue MOB_DAMAGE;
    public static final ModConfigSpec.IntValue MOB_SPAWN_RATE;
    public static final ModConfigSpec.IntValue MINI_BOSS_SPAWN_RATE;
    public static final ModConfigSpec.ConfigValue<Boolean> MINECRAFT_LIKE_SKY;
    public static final ModConfigSpec.ConfigValue<Boolean> COLORED_LIGHT_ENGINE;
    public static final ModConfigSpec.ConfigValue<Boolean> IMMERSIVE_MAGIC;
    public static final ModConfigSpec.IntValue LIGHT_SPREAD;
    public static final ModConfigSpec.IntValue LIGHT_SPREAD_MIN;
    public static final ModConfigSpec.IntValue LIGHT_SPREAD_MAX;
    public static final ModConfigSpec.IntValue LIGHT_SATURATION;
    public static final ModConfigSpec.IntValue LIGHT_BRIGHTNESS;
    public static final ModConfigSpec.IntValue ABYSS_BRIGHTNESS;
    public static final ModConfigSpec.ConfigValue<Boolean> REVERB_EFFECT;
    public static final ModConfigSpec.ConfigValue<Boolean> RESONANT_LIKE_AUDIO;
    public static final ModConfigSpec.IntValue REVERB_STRENGTH;
    public static final ModConfigSpec.IntValue REVERB_LENGTH;
    public static final ModConfigSpec.IntValue ABYSS_HUE;
    public static final ModConfigSpec.IntValue ABYSS_SATURATION;
    public static final ModConfigSpec.IntValue ABYSS_LIGHTNESS;
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_LORES;
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_STORY;

    static {
        BUILDER.push("SomniumBar");
        SOMNIUM_XPOS = BUILDER.comment("Modify the Somnium Bar's position by adjusting the Y and X coordinates.").define("PositionX", 0.0);
        SOMNIUM_YPOS = BUILDER.comment("Modify the Somnium Bar's position by adjusting the Y and X coordinates.").define("PositionY", 0.0);
        BUILDER.pop();
        BUILDER.push("DimensionSettings");
        GUIDE_BOOK = BUILDER.comment(
            "Enable the Guide Book to be automatically provided to each player upon their first join. When enabled, every new player will receive a guide book specifically designed for the Abyss."
        ).define("GuideBook", true);
        PARTICLE_SET = BUILDER.comment("If set to true, particle generation will be reduced, leading to an overall improvement in performance.")
            .define("PotatoParticles", false);
        SHADER_SET = BUILDER.comment("If set to true, internal post shaders will be reduced, resulting in improved performance.")
            .define("PotatoShaders", false);
        OREGEN_SET = BUILDER.comment(
            "Adjust this value between 0 and 30 to modify the frequency of ore vein generation. A setting of 0 corresponds to very rare, while 30 indicates a very common occurrence."
        ).define("OreRarity", 10.0);
        STRUCTURE_SET = BUILDER.comment(
            "Modify this value within the range of 0 to 30 to alter the frequency of structure generation, including dungeons, houses, or giant trees. A setting of 0 makes them rare, while 30 makes them more common."
        ).define("StructureGen", 15.0);
        BUILDER.pop();
        BUILDER.push("Visuals");
        ABYSS_NEBULA_SKY = BUILDER.comment(
            "The Abyss gets a dark, slowly flowing nebula sky with an eclipse instead of the moon; its horizon takes the biome fog colour. Off: the original 1.20.1 sky."
        ).define("AbyssNebulaSky", false);
        VOLUMETRIC_FOG = BUILDER.comment(
            "Drifting 3D noise fog in the biome fog colour in The Abyss and the Frost World: fog banks and wisps you walk through."
        ).define("VolumetricFog", false);
        AMBIENT_SMOKE = BUILDER.comment("Pixel style smoke drifting low over the ground, in the colour of the biome fog. Very cheap.")
            .define("AmbientSmoke", true);
        GROUND_FOG = BUILDER.comment("Soft fog sheets and bands lying on the ground in the colour of the biome fog, in The Abyss and the Frost World.")
            .define("GroundFog", false);
        ROOT_CRACKS = BUILDER.comment("Glowing cracks of the infected roots on the ground of the Nightgrove. Visual only, nothing is placed in the world.")
            .define("RootCracks", true);
        MINECRAFT_LIKE_SKY = BUILDER.comment("The Abyss nebula sky in a pixel style that fits Minecraft: coarse sky pixels, fewer shades, square stars.")
            .define("MinecraftLikeSky", true);
        ABYSS_BRIGHTNESS = BUILDER.comment(
            "Brightness inside The Abyss, like Minecraft's brightness slider: 0 = Moody, 100 = Bright. Only used in The Abyss;",
            "Minecraft's own brightness setting stays untouched and applies everywhere else."
        ).defineInRange("AbyssBrightness", 0, 0, 100);
        COLORED_LIGHT_ENGINE = BUILDER.comment(
            "Coloured light engine (experimental): the mod's luminous blocks spread light in their own colour, block by block like",
            "Minecraft's light (blocked by walls, flows around corners). Runs client-side next to the vanilla light engine, which",
            "stays untouched - other mods and shader packs are not affected."
        ).define("ColoredLightEngine", true);
        IMMERSIVE_MAGIC = BUILDER.comment(
            "Magic lights up its surroundings: blocks, players and mobs near a spell glow in the colour of the spell while it plays."
        ).define("ImmersiveMagic", true);
        LIGHT_SPREAD = BUILDER.comment(
            "Coloured light engine: how far the coloured light reaches, in percent of the block's light level (100 = like",
            "Minecraft's light: a level 12 plant lights 12 blocks)."
        ).defineInRange("LightSpread", 60, 25, 250);
        LIGHT_SPREAD_MIN = BUILDER.comment("Coloured light engine: every light reaches at least this many blocks.")
            .defineInRange("LightSpreadMin", 1, 1, 32);
        LIGHT_SPREAD_MAX = BUILDER.comment("Coloured light engine: no light reaches further than this many blocks.")
            .defineInRange("LightSpreadMax", 6, 2, 48);
        LIGHT_SATURATION = BUILDER.comment(
            "Saturation of the coloured light, percent. 0 = white light, 100 = default."
        ).defineInRange("LightSaturation", 100, 0, 250);
        LIGHT_BRIGHTNESS = BUILDER.comment(
            "Brightness of the coloured light, percent. 100 = default."
        ).defineInRange("LightBrightness", 60, 0, 250);
        BUILDER.push("AbyssColors");
        ABYSS_HUE = BUILDER.comment("Hue of The Abyss dimension.")
            .defineInRange("AbyssHue", 3, -50, 50);
        ABYSS_SATURATION = BUILDER.comment("Saturation of The Abyss dimension.")
            .defineInRange("AbyssSaturation", 0, -50, 50);
        ABYSS_LIGHTNESS = BUILDER.comment("Lightness of The Abyss dimension.")
            .defineInRange("AbyssLightness", -2, -50, 50);
        BUILDER.pop();
        BUILDER.pop();
        BUILDER.push("Gameplay");
        BOSS_HELP = BUILDER.comment(
            "Boss Help: a ring around the crosshair closes in on a parryable blow and meets the inner circle the moment it lands;",
            "it glows cyan while a parry (attack click) counts."
        ).define("BossHelp", true);
        SHADOW_VEIL = BUILDER.comment("Shadow Veil: in The Abyss, sneaking raises a veil of 6 blocks. Hostile creatures outside it cannot see you.",
            "It lasts up to 10 seconds, recharges 10% every 2 seconds and needs 20% to rise again; a creature stepping inside, attacking, shooting or casting breaks it.")
            .define("ShadowVeil", true);
        MOB_HEALTH = BUILDER.comment("Health of the hostile creatures of The Abyss in percent. 100 = original. Suggestion: Normal 90, Easy 75.")
            .defineInRange("MobHealth", 100, 25, 300);
        MOB_DAMAGE = BUILDER.comment("Damage dealt by the hostile creatures of The Abyss in percent. 100 = original. Suggestion: Normal 90, Easy 75.")
            .defineInRange("MobDamage", 100, 25, 300);
        MOB_SPAWN_RATE = BUILDER.comment("Natural spawns of the hostile creatures of The Abyss in percent. 100 = original. Suggestion: Normal 75, Easy 65.")
            .defineInRange("MobSpawnRate", 100, 0, 100);
        MINI_BOSS_SPAWN_RATE = BUILDER.comment("Natural spawns of the mini bosses The Roka, Elder and Crystal Golem in percent. 100 = original. Suggestion: Normal and Easy 75.")
            .defineInRange("MiniBossSpawnRate", 100, 0, 100);
        BUILDER.pop();
        BUILDER.push("Story");
        ENABLE_LORES = BUILDER.comment("The Chronicles of the guide book: pages are noted in the book after events in the game.")
            .define("EnableLores", true);
        ENABLE_STORY = BUILDER.comment("The story of the Rift Of Sins: the call after using the Infected Gauntlet, travelling to the Rift Of Sins,",
            "talking to ??? there and receiving a blessing.").define("EnableStory", true);
        BUILDER.pop();
        BUILDER.push("Audio");
        REVERB_EFFECT = BUILDER.comment("Reverb in The Abyss: entities, footsteps, blocks... echo through the vast dimension (not its music and biome ambience).")
            .define("ReverbEffect", true);
        REVERB_STRENGTH = BUILDER.comment("How loud the reverb is, percent.").defineInRange("ReverbStrength", 100, 0, 100);
        REVERB_LENGTH = BUILDER.comment("How long the reverb lasts, seconds.").defineInRange("ReverbLength", 6, 1, 20);
        RESONANT_LIKE_AUDIO = BUILDER.comment("A finisher sound only you hear when a ring kills a mob (one sound even if several die at once).")
            .define("ResonantLikeAudio", true);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static boolean loresEnabled() {
        try {
            return ENABLE_LORES.get();
        } catch (IllegalStateException notLoadedYet) {
            return true;
        }
    }

    public static boolean storyEnabled() {
        try {
            return ENABLE_STORY.get();
        } catch (IllegalStateException notLoadedYet) {
            return true;
        }
    }
}
