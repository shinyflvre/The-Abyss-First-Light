package net.yezon.theabyss.block.lush;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SporeBlossomBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.ring.fx.GlowParticleOptions;

public class LushSporeBlossomBlock extends SporeBlossomBlock {
    private static final int SPORE = 0xB77BFF;
    private static final int AIR = 0x7CF3E6;

    public LushSporeBlossomBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        level.addParticle(GlowParticleOptions.of(SPORE, 0.07F, 60).gravity(0.0025F).drag(0.98F).flags(GlowParticleOptions.FADE_IN | GlowParticleOptions.SHRINK),
            pos.getX() + random.nextDouble(), pos.getY() + 0.7, pos.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 10; i++) {
            at.set(pos.getX() + Mth.nextInt(random, -10, 10), pos.getY() - random.nextInt(10), pos.getZ() + Mth.nextInt(random, -10, 10));
            if (!level.getBlockState(at).isCollisionShapeFullBlock(level, at)) {
                int colour = random.nextInt(3) == 0 ? AIR : SPORE;
                level.addParticle(GlowParticleOptions.of(colour, 0.04F, 80).drag(0.96F).flags(GlowParticleOptions.FADE_IN | GlowParticleOptions.FLICKER),
                    at.getX() + random.nextDouble(), at.getY() + random.nextDouble(), at.getZ() + random.nextDouble(),
                    (random.nextDouble() - 0.5) * 0.004, -0.002, (random.nextDouble() - 0.5) * 0.004);
            }
        }
    }
}
