package net.yezon.theabyss.platform.common.world;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.yezon.theabyss.platform.services.ServerHooks;

public final class ModifiableBiomeInfo {
    private ModifiableBiomeInfo() {
    }

    public static BiomeInfo get(Biome biome) {
        return ServerHooks.get().biomeInfo(biome);
    }

    public static void setModified(Biome biome, BiomeInfo info) {
        ServerHooks.get().setModifiedBiomeInfo(biome, info);
    }

    public record BiomeInfo(Object climateSettings, BiomeSpecialEffects effects, BiomeGenerationSettings generationSettings, MobSpawnSettings mobSpawnSettings) {
    }
}
