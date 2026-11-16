package net.yezon.theabyss.client.fx.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.secondary.IcePrison;

final class IceFx extends Effect {
    private static final RenderType ICE = RenderTypes.entityTranslucent(Identifier.withDefaultNamespace("textures/block/ice.png"));
    private static final RenderType PACKED = RenderTypes.entityTranslucent(Identifier.withDefaultNamespace("textures/block/packed_ice.png"));
    private static final int COLD = 0x9FE6FF;
    private static final int FROST = 0xDFF8FF;

    private final int target;
    private final Vec3 hand;
    private final int life;
    private AABB box;

    IceFx(SpellFxMessage m) {
        this.target = m.ids().get(0);
        this.hand = SpellFx.vec(m, 0);
        this.life = m.ticks();
    }

    @Override
    public float life() {
        return this.life;
    }

    @Override
    public int outline(Entity entity) {
        return entity.getId() == this.target ? 0xFF9FE6FF : 0;
    }

    private AABB box(Frame f) {
        Entity e = f.level.getEntity(this.target);
        if (e != null) {
            this.box = e.getBoundingBox().inflate(0.28, 0.0, 0.28).expandTowards(0.0, 0.25, 0.0);
        }
        return this.box;
    }

    @Override
    public void draw(Frame f) {
        float age = this.age();
        AABB b = this.box(f);
        if (b == null) {
            return;
        }
        if (age < 5.0F) {
            float k = 1.0F - age / 5.0F;
            f.zap(this.hand, b.getCenter(), 0.35, 0.16F, k, COLD, f.level.getRandom());
        }
        float grow = (float)easeOut(age / IcePrison.FORM);
        float fade = this.fade(0.0F, 3.0F);
        AABB ice = new AABB(b.minX, b.minY, b.minZ, b.maxX, b.minY + (b.maxY - b.minY) * grow, b.maxZ);
        Vec3 cam = f.camera;
        AABB core = ice.deflate(0.06, 0.0, 0.06);
        f.mesh(PACKED, (pose, buffer, camera) -> block(pose, buffer, core, cam, Math.round(120 * fade), 0xF2FBFF));
        f.mesh(ICE, (pose, buffer, camera) -> block(pose, buffer, ice, cam, Math.round(230 * fade), 0xFFFFFF));
        f.flare(ice.getCenter(), (float)ice.getYsize() * 1.6F, COLD, 0.25F * fade);
        Vec3[] c = corners(ice);
        int[][] edges = {{0, 1}, {1, 3}, {3, 2}, {2, 0}, {4, 5}, {5, 7}, {7, 6}, {6, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
        for (int[] e : edges) {
            f.line(c[e[0]], c[e[1]], 0.08F, 0.7F * fade, COLD);
        }
        float crack = Math.max(0.0F, (age - (this.life - 20)) / 20.0F);
        if (crack > 0.0F) {
            RandomSource random = RandomSource.create(this.target * 31L + (long)(age / 3.0F));
            for (int k = 0; k < (int)(2 + crack * 8); k++) {
                Vec3 a = new Vec3(ice.minX + random.nextDouble() * ice.getXsize(), ice.minY + random.nextDouble() * ice.getYsize(), random.nextBoolean() ? ice.minZ : ice.maxZ);
                Vec3 d = new Vec3(random.nextGaussian() * 0.5, random.nextGaussian() * 0.5, 0.0);
                f.zap(a, a.add(d), 0.12, 0.04F, crack, FROST, random);
            }
        }
    }

    private static void block(PoseStack.Pose pose, VertexConsumer buffer, AABB b, Vec3 camera, int alpha, int rgb) {
        int colour = alpha << 24 | rgb;
        double[] xs = cuts(b.minX, b.maxX);
        double[] ys = cuts(b.minY, b.maxY);
        double[] zs = cuts(b.minZ, b.maxZ);
        for (int i = 0; i + 1 < xs.length; i++) {
            for (int j = 0; j + 1 < ys.length; j++) {
                cell(buffer, pose, camera, new Vec3(xs[i], ys[j], b.minZ), new Vec3(xs[i + 1], ys[j], b.minZ), new Vec3(xs[i + 1], ys[j + 1], b.minZ),
                    new Vec3(xs[i], ys[j + 1], b.minZ), colour);
                cell(buffer, pose, camera, new Vec3(xs[i + 1], ys[j], b.maxZ), new Vec3(xs[i], ys[j], b.maxZ), new Vec3(xs[i], ys[j + 1], b.maxZ),
                    new Vec3(xs[i + 1], ys[j + 1], b.maxZ), colour);
            }
        }
        for (int k = 0; k + 1 < zs.length; k++) {
            for (int j = 0; j + 1 < ys.length; j++) {
                cell(buffer, pose, camera, new Vec3(b.minX, ys[j], zs[k + 1]), new Vec3(b.minX, ys[j], zs[k]), new Vec3(b.minX, ys[j + 1], zs[k]),
                    new Vec3(b.minX, ys[j + 1], zs[k + 1]), colour);
                cell(buffer, pose, camera, new Vec3(b.maxX, ys[j], zs[k]), new Vec3(b.maxX, ys[j], zs[k + 1]), new Vec3(b.maxX, ys[j + 1], zs[k + 1]),
                    new Vec3(b.maxX, ys[j + 1], zs[k]), colour);
            }
        }
        for (int i = 0; i + 1 < xs.length; i++) {
            for (int k = 0; k + 1 < zs.length; k++) {
                cell(buffer, pose, camera, new Vec3(xs[i], b.maxY, zs[k]), new Vec3(xs[i + 1], b.maxY, zs[k]), new Vec3(xs[i + 1], b.maxY, zs[k + 1]),
                    new Vec3(xs[i], b.maxY, zs[k + 1]), colour);
            }
        }
    }

    private static void cell(VertexConsumer buffer, PoseStack.Pose pose, Vec3 camera, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int colour) {
        Frame.quad(buffer, pose, a.subtract(camera), b.subtract(camera), c.subtract(camera), d.subtract(camera), 0.0F, 0.0F, 1.0F, 1.0F, colour);
    }

    private static double[] cuts(double min, double max) {
        int n = Math.max(1, (int)Math.ceil(max - min - 1.0E-6));
        double[] out = new double[n + 1];
        for (int i = 0; i <= n; i++) {
            out[i] = min + (max - min) * i / n;
        }
        return out;
    }

    private static Vec3[] corners(AABB b) {
        Vec3[] c = new Vec3[8];
        for (int i = 0; i < 8; i++) {
            c[i] = new Vec3((i & 1) == 0 ? b.minX : b.maxX, (i & 4) == 0 ? b.minY : b.maxY, (i & 2) == 0 ? b.minZ : b.maxZ);
        }
        return c;
    }

    @Override
    public void tick(ClientLevel level) {
        if (this.box == null || this.age() > this.life - 2) {
            return;
        }
        if (this.age() < 5.0F) {
            TrailStreaks.emit(this.hand, this.box.getCenter(), COLD, 4);
        }
        RandomSource random = level.getRandom();
        if (random.nextInt(3) == 0) {
            Vec3 at = new Vec3(this.box.minX + random.nextDouble() * this.box.getXsize(), this.box.maxY, this.box.minZ + random.nextDouble() * this.box.getZsize());
            level.addParticle(ParticleTypes.SNOWFLAKE, at.x, at.y, at.z, 0.0, 0.02, 0.0);
        }
        if (random.nextInt(2) == 0) {
            Vec3 at = new Vec3(this.box.minX + random.nextDouble() * this.box.getXsize(), this.box.minY + 0.1, this.box.minZ + random.nextDouble() * this.box.getZsize());
            emit(level, smoke(0xE8F8FF, 0.5F, 26).gravity(-0.002F).drag(0.9F), at, new Vec3(random.nextGaussian() * 0.02, 0.01, random.nextGaussian() * 0.02));
        }
    }

    static final class Burst extends Effect {
        private final Vec3 at;
        private final Vec3[] shards;
        private boolean burst;

        Burst(SpellFxMessage m) {
            this.at = SpellFx.vec(m, 0);
            RandomSource random = RandomSource.create((long)(this.at.x * 17 + this.at.z * 29));
            this.shards = new Vec3[18];
            for (int i = 0; i < this.shards.length; i++) {
                this.shards[i] = new Vec3(random.nextGaussian(), random.nextGaussian() * 0.7 + 0.2, random.nextGaussian()).normalize();
            }
        }

        @Override
        public float life() {
            return 14.0F;
        }

        @Override
        public void draw(Frame f) {
            float age = this.age();
            float k = 1.0F - age / this.life();
            double e = easeOut(age / 9.0);
            for (Vec3 d : this.shards) {
                Vec3 a = this.at.add(d.scale(0.3 + 2.6 * e));
                f.line(a, a.add(d.scale(0.6 * k + 0.1)), 0.1F * k + 0.02F, k, COLD);
            }
            f.flare(this.at, 4.5F * k, FROST, k);
        }

        @Override
        public void tick(ClientLevel level) {
            if (this.burst) {
                return;
            }
            this.burst = true;
            RandomSource random = level.getRandom();
            for (int k = 0; k < 24; k++) {
                level.addParticle(ParticleTypes.SNOWFLAKE, this.at.x, this.at.y, this.at.z, random.nextGaussian() * 0.2, random.nextDouble() * 0.2, random.nextGaussian() * 0.2);
            }
        }
    }
}
