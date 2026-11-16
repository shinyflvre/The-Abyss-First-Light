package net.yezon.theabyss.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.client.fx.ScreenFx;
import org.joml.Quaternionf;

public class GroundFogParticle extends SingleQuadParticle {
    private static final SingleQuadParticle.Layer LAYER = new SingleQuadParticle.Layer(
        true, net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_PARTICLES, AmbientSmokeParticle.PIPELINE,
        net.minecraft.client.renderer.RenderPipelines.OIT_PARTICLE);
    private static final java.util.List<GroundFogParticle> LIVE = new java.util.ArrayList<>();
    private static final float RANGE = 48.0F;
    private static final float EDGE = 12.0F;
    private static final float DISTANCE_SCALING = 0.08F;
    private static final float BAND_TOP = 0.35F;
    private static final float BAND_GROWTH = 0.05F;
    private static final float QUARTER_PI = (float)(Math.PI / 4.0);

    private final boolean band;
    private final float spin;
    private final float peakAlpha;
    private float fade;
    private long lastTick;

    public static GroundFogParticleProvider provider(SpriteSet spriteSet) {
        return new GroundFogParticleProvider(spriteSet);
    }

    public static int alive(ClientLevel current) {
        long now = current.getGameTime();
        LIVE.removeIf(fog -> !fog.isAlive() || fog.level != current || Math.abs(now - fog.lastTick) > 5L);
        return LIVE.size();
    }

    protected GroundFogParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());
        this.setSprite(sprites.get(this.random));
        this.lifetime = 200 + this.random.nextInt(100);
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.xd = vx;
        this.yd = 0.0;
        this.zd = vz;
        this.band = this.random.nextBoolean();
        this.spin = this.random.nextInt(4) * (float)(Math.PI / 2.0);
        this.quadSize = 1.7F + this.random.nextFloat() * 0.6F;
        this.peakAlpha = 0.30F + this.random.nextFloat() * 0.12F;
        this.alpha = 0.0F;
        this.updateColour();
        this.lastTick = level.getGameTime();
        LIVE.add(this);
    }

    @Override
    public void tick() {
        this.lastTick = this.level.getGameTime();
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.move(this.xd, 0.0, this.zd);
        this.updateColour();
        Vec3 camera = Minecraft.getInstance().gameRenderer.mainCamera().position();
        double dx = this.x - camera.x;
        double dz = this.z - camera.z;
        if (dx * dx + dz * dz > (RANGE + 4.0) * (RANGE + 4.0)) {
            this.remove();
            return;
        }
        float fadeIn = Math.min(1.0F, this.age / 40.0F);
        float fadeOut = Math.min(1.0F, (this.lifetime - this.age) / 60.0F);
        this.fade = fadeIn * fadeOut;
    }

    private void updateColour() {
        int fog = ScreenFx.fogColour();
        this.setColor(lift(ARGB.red(fog)), lift(ARGB.green(fog)), lift(ARGB.blue(fog)));
    }

    private static float lift(int channel) {
        return Math.min(1.0F, channel / 255.0F * 2.0F + 0.06F);
    }

    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTick) {
        Vec3 pos = camera.position();
        float dx = (float)(Mth.lerp(partialTick, this.xo, this.x) - pos.x);
        float dy = (float)(Mth.lerp(partialTick, this.yo, this.y) - pos.y);
        float dz = (float)(Mth.lerp(partialTick, this.zo, this.z) - pos.z);
        float horizontal = (float)Math.sqrt(dx * dx + dz * dz);
        float distance = (float)Math.sqrt(horizontal * horizontal + dy * dy);
        float size = this.quadSize * (1.0F + distance * DISTANCE_SCALING);
        float pitch = (float)Math.atan2(Math.abs(dy), horizontal);
        float edge = Mth.clamp((RANGE - horizontal) / EDGE, 0.0F, 1.0F);
        float view;
        float near;
        if (this.band) {
            view = 1.0F - Math.min(pitch, QUARTER_PI) / QUARTER_PI;
            near = Mth.clamp((distance - 1.0F) / 3.0F, 0.0F, 1.0F);
        } else {
            view = Math.min(pitch, 0.4F) / 0.4F;
            near = 1.0F;
        }
        this.alpha = this.peakAlpha * this.fade * edge * view * near;
        if (this.alpha <= 0.004F) {
            return;
        }
        Quaternionf rotation = new Quaternionf();
        float y = dy;
        if (this.band) {
            SingleQuadParticle.FacingCameraMode.LOOKAT_Y.setRotation(rotation, camera, partialTick);
            y = dy + BAND_TOP * (1.0F + distance * BAND_GROWTH) - size * 0.55F;
        } else {
            rotation.rotationX((float)(-Math.PI / 2.0)).rotateZ(this.spin);
        }
        state.add(this.getLayer(), dx, y, dz, rotation.x, rotation.y, rotation.z, rotation.w, size, this.getU0(), this.getU1(), this.getV0(),
            this.getV1(), ARGB.colorFromFloat(this.alpha, this.rCol, this.gCol, this.bCol), this.getLightCoords(partialTick));
    }

    @Override
    public int getLightCoords(float partialTick) {
        return 15728880;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return LAYER;
    }

    public static class GroundFogParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public GroundFogParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(
            SimpleParticleType type, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random
        ) {
            return new GroundFogParticle(level, x, y, z, vx, vy, vz, this.spriteSet);
        }
    }
}
