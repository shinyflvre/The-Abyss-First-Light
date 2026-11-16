package net.yezon.theabyss.world.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.material.rule.RuleEvaluator;

public record SolidTerrainRule(DensityFunction density, MaterialRule rule) implements MaterialRule {
    public static final MapCodec<SolidTerrainRule> CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(
                DensityFunction.CODEC.fieldOf("density").forGetter(SolidTerrainRule::density),
                MaterialRule.CODEC.fieldOf("rule").forGetter(SolidTerrainRule::rule)
            )
            .apply(i, SolidTerrainRule::new)
    );

    @Override
    public MapCodec<SolidTerrainRule> codec() {
        return CODEC;
    }

    @Override
    public RuleEvaluator compile(MaterialRuleContext context) {
        RuleEvaluator inner = this.rule.compile(context);
        MaterialRules.DensityGetter density = context.getDensitiesInChunk(this.density, false);
        return (blockX, blockY, blockZ) -> {
            var state = inner.tryApply(blockX, blockY, blockZ);
            return state != null && density.get() > 0.0F ? state : null;
        };
    }
}
