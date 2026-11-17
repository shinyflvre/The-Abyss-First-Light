package net.yezon.theabyss.block.lush;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.init.TheabyssModParticleTypes;

public class LushAzaleaLeavesBlock extends LeavesBlock {
    public static final MapCodec<LushAzaleaLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.floatRange(0.0F, 1.0F).fieldOf("leaf_particle_chance").forGetter(block -> block.leafParticleChance),
        ExtraCodecs.ARGB_COLOR_CODEC.fieldOf("leaf_particle_color").forGetter(block -> block.leafParticleColor),
        propertiesCodec()
    ).apply(instance, LushAzaleaLeavesBlock::new));
    private final float leafParticleChance;
    private final int leafParticleColor;
    private ColorParticleOption leafParticle;

    public LushAzaleaLeavesBlock(float leafParticleChance, int leafParticleColor, BlockBehaviour.Properties properties) {
        super(properties);
        this.leafParticleChance = leafParticleChance;
        this.leafParticleColor = leafParticleColor;
    }

    @Override
    public MapCodec<? extends LushAzaleaLeavesBlock> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        if (!(random.nextFloat() >= this.leafParticleChance) && !isFaceFull(belowState.getCollisionShape(level, below), Direction.UP)) {
            ParticleUtils.spawnParticleBelow(level, pos, random, this.leafParticle());
        }
    }

    private ColorParticleOption leafParticle() {
        if (this.leafParticle == null) {
            this.leafParticle = ColorParticleOption.create(TheabyssModParticleTypes.TINTED_LEAVES.get(), this.leafParticleColor);
        }
        return this.leafParticle;
    }
}
