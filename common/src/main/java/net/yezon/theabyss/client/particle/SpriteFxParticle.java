package net.yezon.theabyss.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.yezon.theabyss.compat.ARGB;
import net.minecraft.util.Mth;
import net.yezon.theabyss.ring.fx.FxOptions;
import org.joml.Quaternionf;

public class SpriteFxParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final int flags;
    private final float drag;
    private final float startSize;
    private final float red;
    private final float green;
    private final float blue;
    private final boolean mirrored;
    private float bounciness = 0.6F;
    private int loopFrames;
    private int loopTicks = 1;
    private int loopPhase;

    protected SpriteFxParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, FxOptions options, SpriteSet sprites) {
        super(level, x, y, z);
        this.setSprite(sprites.get(0, 1));
        this.sprites = sprites;
        this.flags = options.flags();
        this.drag = options.drag();
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.gravity = options.gravity();
        this.hasPhysics = (this.flags & FxOptions.BOUNCE) != 0;
        int life = options.lifetime();
        this.lifetime = Math.max(1, life + this.random.nextInt(Math.max(1, life / 4 + 1)) - life / 8);
        this.startSize = options.size() * (0.85F + this.random.nextFloat() * 0.3F);
        this.quadSize = this.startSize;
        int c = options.color();
        this.red = ARGB.red(c) / 255.0F;
        this.green = ARGB.green(c) / 255.0F;
        this.blue = ARGB.blue(c) / 255.0F;
        this.mirrored = this.random.nextBoolean();
        this.roll = (this.flags & FxOptions.FLAT) != 0 ? 0.0F : 0.0F;
        if ((this.flags & (FxOptions.ANIMATE | FxOptions.FLICKER)) == 0) {
            this.setSprite(sprites.get(this.random));
        } else {
            this.setSpriteFromAge(sprites);
        }
        this.applyColour(0.0F);
        this.alpha = 1.0F;
    }

    private void startLoop(int frames, int ticks, boolean freeRoll) {
        this.loopFrames = frames;
        this.loopTicks = ticks;
        this.loopPhase = this.random.nextInt(frames);
        if (freeRoll) {
            this.roll = this.random.nextFloat() * (float)(Math.PI * 2.0);
            this.oRoll = this.roll;
        }
        this.setSprite(this.sprites.get(this.loopPhase, frames - 1));
        this.alpha = 0.0F;
    }

    private void applyColour(float t) {
        if ((this.flags & FxOptions.HOT) != 0) {
            float cool = Mth.clamp(t * 3.0F, 0.0F, 1.0F);
            this.setColor(Mth.lerp(cool, 1.0F, this.red), Mth.lerp(cool, 1.0F, this.green), Mth.lerp(cool, 1.0F, this.blue));
        } else {
            this.setColor(this.red, this.green, this.blue);
        }
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.yd -= this.gravity;
        this.move(this.xd, this.yd, this.zd);
        if ((this.flags & FxOptions.BOUNCE) != 0 && this.onGround) {
            this.yd = Math.abs(this.yd) * this.bounciness + 0.02;
            this.bounciness *= 0.7F;
            this.xd *= 0.7;
            this.zd *= 0.7;
        }
        this.xd *= this.drag;
        this.yd *= this.drag;
        this.zd *= this.drag;
        float t = this.age / (float)this.lifetime;
        if (this.loopFrames > 0) {
            this.oRoll = this.roll;
            this.setSprite(this.sprites.get((this.age / this.loopTicks + this.loopPhase) % this.loopFrames, this.loopFrames - 1));
            this.alpha = Math.min(1.0F, this.age / 2.0F) * Mth.clamp((1.0F - t) / 0.35F, 0.0F, 1.0F);
        } else if ((this.flags & FxOptions.ANIMATE) != 0) {
            this.setSpriteFromAge(this.sprites);
        } else if ((this.flags & FxOptions.FLICKER) != 0 && this.age % 2 == 0) {
            this.setSprite(this.sprites.get(this.random));
        }
        float size = 1.0F;
        if ((this.flags & FxOptions.SHRINK) != 0) {
            size *= 1.0F - t * t;
        }
        if ((this.flags & FxOptions.GROW) != 0) {
            size *= (this.flags & FxOptions.FLAT) != 0 ? 1.0F - (1.0F - t) * (1.0F - t) : 1.0F + 1.2F * t;
        }
        this.quadSize = this.startSize * Math.max(size, 0.02F);
        this.applyColour(t);
        if ((this.flags & (FxOptions.SMOKE | FxOptions.FLAT)) != 0) {
            this.alpha = Mth.clamp((1.0F - t) * 1.3F, 0.0F, 1.0F) * ((this.flags & FxOptions.SMOKE) != 0 ? 0.6F : 1.0F);
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        if ((this.flags & FxOptions.FLAT) == 0) {
            super.render(buffer, camera, partialTick);
            return;
        }
        this.renderRotatedQuad(buffer, camera, new Quaternionf().rotationX((float)(-Math.PI / 2.0)), partialTick);
        this.renderRotatedQuad(buffer, camera, new Quaternionf().rotationX((float)(Math.PI / 2.0)), partialTick);
    }

    @Override
    protected float getU0() {
        return this.mirrored && (this.flags & FxOptions.FLAT) == 0 ? super.getU1() : super.getU0();
    }

    @Override
    protected float getU1() {
        return this.mirrored && (this.flags & FxOptions.FLAT) == 0 ? super.getU0() : super.getU1();
    }

    @Override
    public int getLightColor(float partialTick) {
        return (this.flags & FxOptions.SMOKE) != 0 ? super.getLightColor(partialTick) : 15728880;
    }

    public net.minecraft.world.phys.AABB cullBox() {
        double r = Math.max(this.quadSize, 0.25F);
        return new net.minecraft.world.phys.AABB(this.x - r, this.y - r, this.z - r, this.x + r, this.y + r, this.z + r);
    }

    public net.minecraft.world.phys.AABB getRenderBoundingBox(float partialTicks) {
        return this.cullBox();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return this.loopFrames > 0 || (this.flags & (FxOptions.SMOKE | FxOptions.FLAT)) != 0 ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT : ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static Provider provider(SpriteSet sprites) {
        return new Provider(sprites);
    }

    public static Provider looping(SpriteSet sprites, int frames, int ticks, boolean freeRoll) {
        return new Provider(sprites, frames, ticks, freeRoll);
    }

    public static class Provider implements ParticleProvider<FxOptions> {
        private final SpriteSet sprites;
        private final int frames;
        private final int ticks;
        private final boolean freeRoll;

        public Provider(SpriteSet sprites) {
            this(sprites, 0, 1, false);
        }

        Provider(SpriteSet sprites, int frames, int ticks, boolean freeRoll) {
            this.sprites = sprites;
            this.frames = frames;
            this.ticks = ticks;
            this.freeRoll = freeRoll;
        }

        @Override
        public Particle createParticle(FxOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            SpriteFxParticle particle = new SpriteFxParticle(level, x, y, z, vx, vy, vz, options, this.sprites);
            if (this.frames > 0) {
                particle.startLoop(this.frames, this.ticks, this.freeRoll);
            }
            return particle;
        }
    }
}
