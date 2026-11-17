package net.yezon.theabyss.client.particle;

import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

public class FluidDripParticle extends TextureSheetParticle {
    private static final int HANG_TICKS = 40;
    private static final float COOL_END = 16.0F / (HANG_TICKS + 16.0F);

    public enum Stage {
        HANG, FALL, LAND
    }

    private final Stage stage;
    private final Fluid fluid;
    private final @Nullable ParticleOptions next;
    private final int hot;
    private final int cool;

    public static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites, Stage stage, Supplier<? extends Fluid> fluid,
                                                                @Nullable Supplier<? extends ParticleOptions> next, int hot, int cool) {
        return (type, level, x, y, z, vx, vy, vz) -> new FluidDripParticle(level, x, y, z, sprites, stage, fluid.get(),
            next == null ? null : next.get(), hot, cool);
    }

    protected FluidDripParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, Stage stage, Fluid fluid,
                                @Nullable ParticleOptions next, int hot, int cool) {
        super(level, x, y, z);
        this.setSprite(sprites.get(0, 1));
        this.stage = stage;
        this.fluid = fluid;
        this.next = next;
        this.hot = hot;
        this.cool = cool;
        this.setSize(0.01F, 0.01F);
        this.gravity = 0.06F;
        switch (stage) {
            case HANG -> {
                this.gravity *= 0.02F;
                this.lifetime = HANG_TICKS;
                this.tint(1.0F);
            }
            case FALL -> {
                this.lifetime = (int)(64.0 / (this.random.nextFloat() * 0.8 + 0.2));
                this.tint(0.0F);
            }
            case LAND -> {
                this.lifetime = (int)(16.0 / (this.random.nextFloat() * 0.8 + 0.2));
                this.tint(0.0F);
            }
        }
    }

    private void tint(float heat) {
        this.setColor(mix(this.cool >> 16, this.hot >> 16, heat), mix(this.cool >> 8, this.hot >> 8, heat), mix(this.cool, this.hot, heat));
    }

    private static float mix(int a, int b, float t) {
        float from = (a & 255) / 255.0F;
        float to = (b & 255) / 255.0F;
        return from + (to - from) * t;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.stage == Stage.HANG) {
            float curve = 16.0F / (HANG_TICKS - this.lifetime + 16.0F);
            this.tint(Math.max(0.0F, (curve - COOL_END) / (1.0F - COOL_END)));
        }
        if (this.lifetime-- <= 0) {
            this.remove();
            if (this.stage == Stage.HANG && this.next != null) {
                this.level.addParticle(this.next, this.x, this.y, this.z, this.xd, this.yd, this.zd);
            }
            return;
        }
        this.yd = this.yd - this.gravity;
        this.move(this.xd, this.yd, this.zd);
        if (this.stage == Stage.HANG) {
            this.xd *= 0.02;
            this.yd *= 0.02;
            this.zd *= 0.02;
        } else if (this.stage == Stage.FALL && this.onGround) {
            this.remove();
            if (this.next != null) {
                this.level.addParticle(this.next, this.x, this.y, this.z, 0.0, 0.0, 0.0);
            }
            return;
        }
        this.xd *= 0.98F;
        this.yd *= 0.98F;
        this.zd *= 0.98F;
        BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
        FluidState state = this.level.getFluidState(pos);
        if (state.getType().isSame(this.fluid) && this.y < pos.getY() + state.getHeight(this.level, pos)) {
            this.remove();
        }
    }
}
