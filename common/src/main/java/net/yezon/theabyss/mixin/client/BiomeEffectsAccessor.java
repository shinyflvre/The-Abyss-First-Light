package net.yezon.theabyss.mixin.client;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Biome.class)
public interface BiomeEffectsAccessor {
    @Mutable
    @Accessor("specialEffects")
    void theabyss$setSpecialEffects(BiomeSpecialEffects effects);
}
