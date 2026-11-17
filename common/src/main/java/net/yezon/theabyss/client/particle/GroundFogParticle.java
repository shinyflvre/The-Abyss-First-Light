package net.yezon.theabyss.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.yezon.theabyss.compat.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.client.fx.ScreenFx;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class GroundFogParticle extends TextureSheetParticle {
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
        super(level, x, y, z);
        this.setSprite(sprites.get(0, 1));
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
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
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
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        Vec3 pos = camera.getPosition();
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
        int light = this.getLightColor(partialTick);
        this.quad(buffer, rotation, dx, y, dz, size, light, false);
        if (!this.band) {
            this.quad(buffer, rotation, dx, y, dz, size, light, true);
        }
    }

    private void quad(VertexConsumer buffer, Quaternionf rotation, float x, float y, float z, float size, int light, boolean back) {
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        if (back) {
            this.vertex(buffer, rotation, x, y, z, -1.0F, -1.0F, size, u0, v1, light);
            this.vertex(buffer, rotation, x, y, z, -1.0F, 1.0F, size, u0, v0, light);
            this.vertex(buffer, rotation, x, y, z, 1.0F, 1.0F, size, u1, v0, light);
            this.vertex(buffer, rotation, x, y, z, 1.0F, -1.0F, size, u1, v1, light);
        } else {
            this.vertex(buffer, rotation, x, y, z, 1.0F, -1.0F, size, u1, v1, light);
            this.vertex(buffer, rotation, x, y, z, 1.0F, 1.0F, size, u1, v0, light);
            this.vertex(buffer, rotation, x, y, z, -1.0F, 1.0F, size, u0, v0, light);
            this.vertex(buffer, rotation, x, y, z, -1.0F, -1.0F, size, u0, v1, light);
        }
    }

    private void vertex(VertexConsumer buffer, Quaternionf rotation, float x, float y, float z, float cornerX, float cornerY, float size, float u, float v, int light) {
        Vector3f corner = new Vector3f(cornerX, cornerY, 0.0F).rotate(rotation).mul(size).add(x, y, z);
        buffer.addVertex(corner.x(), corner.y(), corner.z()).setUv(u, v).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return AbyssParticleRenderTypes.SOFT;
    }

    public static class GroundFogParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public GroundFogParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(
            SimpleParticleType type, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            return new GroundFogParticle(level, x, y, z, vx, vy, vz, this.spriteSet);
        }
    }
}
