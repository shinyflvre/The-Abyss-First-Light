package net.yezon.theabyss.world.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record SolidTerrainRule(DensityFunction density, SurfaceRules.RuleSource rule) {
    private static final MapCodec<SolidTerrainRule> RECORD_CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(
                DensityFunction.HOLDER_HELPER_CODEC.fieldOf("density").forGetter(SolidTerrainRule::density),
                SurfaceRules.RuleSource.CODEC.fieldOf("rule").forGetter(SolidTerrainRule::rule)
            )
            .apply(i, SolidTerrainRule::new)
    );
    public static final MapCodec<SurfaceRules.RuleSource> CODEC = RECORD_CODEC.xmap(
        SolidTerrainRule::rule, rule -> new SolidTerrainRule(DensityFunctions.zero(), rule)
    );
}
