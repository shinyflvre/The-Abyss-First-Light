package net.yezon.theabyss.client.fx.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.fx.FxOptions;
import net.yezon.theabyss.ring.secondary.TidalWave;

final class TidalFx extends Effect {
    private static final RenderType WATER = RenderTypes.entityTranslucent(Identifier.withDefaultNamespace("textures/block/water_still.png"));
    private static final double[][] PROFILE = {
        {-3.6, 0.0}, {-2.8, 0.22}, {-2.0, 0.62}, {-1.3, 1.22}, {-0.7, 1.86}, {-0.2, 2.38}, {0.3, 2.74},
        {0.75, 2.82}, {1.1, 2.6}, {1.2, 2.25}, {1.0, 1.95}
    };
    private static final int CREST = 7;
    private static final int DEEP = 0x0E3C9A;
    private static final int SHALLOW = 0x2FA6E6;
    private static final int FOAM = 0xE6F8FF;
    private static final float BREAK = 12.0F;
    private static final double SCALE = 1.35;

    private final Vec3 origin;
    private final Vec3 dir;
    private final Vec3 side;
    private final int duration;
    private boolean crashed;

    TidalFx(SpellFxMessage m) {
        this.origin = SpellFx.vec(m, 0);
        this.dir = new Vec3(m.data().get(3), 0.0, m.data().get(4)).normalize();
        this.side = new Vec3(-this.dir.z, 0.0, this.dir.x);
        this.duration = m.ticks();
    }

    @Override
    public float life() {
        return this.duration + BREAK;
    }

    private double front(float age) {
        return TidalWave.front(Math.min(age, this.duration) + Math.max(0.0F, age - this.duration) * 0.4);
    }

    private double height(float age) {
        double rise = easeOut(age / 5.0);
        double fall = age < this.duration ? 1.0 : 1.0 - smooth((age - this.duration) / BREAK);
        return rise * fall;
    }

    private Vec3 point(float age, double front, double half, int row, double s) {
        double k = Math.sqrt(Math.max(0.0, 1.0 - (s / half) * (s / half)));
        double h = this.height(age);
        double pitch = age < this.duration ? 0.0 : smooth((age - this.duration) / BREAK) * 1.6;
        double px = (PROFILE[row][0] + pitch * PROFILE[row][1] / 2.8) * SCALE;
        double py = PROFILE[row][1] * k * h * SCALE;
        double wobble = 0.14 * Math.sin(s * 1.3 + age * 0.55 + row * 0.4) * (PROFILE[row][1] / 2.8) * h;
        return this.origin.add(this.dir.scale(front + px * (0.6 + 0.4 * k))).add(this.side.scale(s)).add(0.0, py + wobble, 0.0);
    }

    @Override
    public void draw(Frame f) {
        net.yezon.theabyss.client.fx.MagicLights.add(this.point(this.age(), this.front(this.age()), Math.max(0.1, net.yezon.theabyss.ring.secondary.TidalWave.halfWidth(this.front(this.age()))), CREST, 0.0).add(0.0, 0.5, 0.0), SHALLOW, 9.0F, this.fade(4.0F, 8.0F));
        float age = this.age();
        double front = this.front(age);
        double half = TidalWave.halfWidth(front);
        int columns = Math.max(10, (int)(half * 2.4));
        int rows = PROFILE.length;
        Vec3[][] grid = new Vec3[columns + 1][rows];
        for (int c = 0; c <= columns; c++) {
            double s = -half + 2.0 * half * c / columns;
            for (int r = 0; r < rows; r++) {
                grid[c][r] = this.point(age, front, half, r, s);
            }
        }
        float alpha = (float)Math.min(1.0, age / 3.0) * (float)(age < this.duration ? 1.0 : 1.0 - (age - this.duration) / BREAK);
        int frame = (int)(age / 2.0F) % 32;
        Vec3 cam = f.camera;
        f.mesh(WATER, (pose, buffer, camera) -> sheet(pose, buffer, grid, cam, frame, alpha));
        Vec3[] crest = new Vec3[columns + 1];
        Vec3[] foot = new Vec3[columns + 1];
        for (int c = 0; c <= columns; c++) {
            crest[c] = grid[c][CREST];
            foot[c] = grid[c][0].add(0.0, 0.05, 0.0);
        }
        f.ribbon(crest, 0.35F, 0.55F * alpha, FOAM);
        f.ribbon(foot, 0.6F, 0.35F * alpha, FOAM);
    }

    private static void sheet(PoseStack.Pose pose, VertexConsumer buffer, Vec3[][] grid, Vec3 camera, int frame, float alpha) {
        int columns = grid.length - 1;
        int rows = grid[0].length;
        float v0 = frame / 32.0F;
        float dv = 1.0F / 32.0F;
        for (int c = 0; c < columns; c++) {
            for (int r = 0; r + 1 < rows; r++) {
                int a0 = colour(r, rows, alpha);
                int a1 = colour(r + 1, rows, alpha);
                float va = v0 + dv * r / (rows - 1);
                float vb = v0 + dv * (r + 1) / (rows - 1);
                Frame.vertex(buffer, pose, grid[c][r].subtract(camera), 0.0F, va, a0);
                Frame.vertex(buffer, pose, grid[c + 1][r].subtract(camera), 1.0F, va, a0);
                Frame.vertex(buffer, pose, grid[c + 1][r + 1].subtract(camera), 1.0F, vb, a1);
                Frame.vertex(buffer, pose, grid[c][r + 1].subtract(camera), 0.0F, vb, a1);
            }
        }
    }

    private static int colour(int row, int rows, float alpha) {
        float t = Math.min(1.0F, row / (float)CREST);
        int rgb = row >= CREST ? FOAM : lerp(DEEP, SHALLOW, t);
        int a = Math.round(255 * alpha * (0.86F + 0.12F * t));
        return a << 24 | rgb;
    }

    private static int lerp(int a, int b, float t) {
        int r = Math.round(((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = Math.round(((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = Math.round((a & 255) * (1 - t) + (b & 255) * t);
        return r << 16 | g << 8 | bl;
    }

    @Override
    public void tick(ClientLevel level) {
        float age = this.age();
        RandomSource random = level.getRandom();
        double front = this.front(age);
        double half = TidalWave.halfWidth(front);
        if (age < this.duration) {
            FxOptions droplet = orb(0x8FEAFF, 0.12F, 30).gravity(0.04F).flags(FxOptions.SHRINK | FxOptions.BOUNCE);
            for (int k = 0; k < 8; k++) {
                double s = (random.nextDouble() * 2.0 - 1.0) * half;
                Vec3 top = this.point(age, front, half, CREST, s);
                level.addParticle(ParticleTypes.SPLASH, top.x, top.y, top.z, this.dir.x * 0.3, 0.2, this.dir.z * 0.3);
                if (k % 2 == 0) {
                    emit(level, droplet, top, this.dir.scale(0.25 + random.nextDouble() * 0.2).add(0.0, 0.15 + random.nextDouble() * 0.15, 0.0));
                }
            }
            for (int k = 0; k < 3; k++) {
                double s = (random.nextDouble() * 2.0 - 1.0) * half;
                Vec3 foot = this.point(age, front, half, 1, s);
                level.addParticle(ParticleTypes.BUBBLE_POP, foot.x, foot.y + 0.2, foot.z, 0.0, 0.05, 0.0);
            }
            if (random.nextInt(3) == 0) {
                double s = (random.nextDouble() * 2.0 - 1.0) * half;
                emit(level, smoke(0xE8F8FF, 0.45F, 18).gravity(-0.003F).drag(0.9F), this.point(age, front, half, CREST, s), this.dir.scale(0.05));
            }
        } else if (!this.crashed) {
            this.crashed = true;
            Vec3 c = this.origin.add(this.dir.scale(front + 1.0));
            for (int k = 0; k < 60; k++) {
                double s = (random.nextDouble() * 2.0 - 1.0) * half;
                Vec3 at = c.add(this.side.scale(s)).add(0.0, random.nextDouble() * 1.5, 0.0);
                level.addParticle(ParticleTypes.SPLASH, at.x, at.y, at.z, this.dir.x * 0.4, 0.3, this.dir.z * 0.4);
                if (k % 3 == 0) {
                    emit(level, orb(0x8FEAFF, 0.14F, 34).gravity(0.04F).flags(FxOptions.SHRINK | FxOptions.BOUNCE), at,
                        this.dir.scale(0.3).add(random.nextGaussian() * 0.12, 0.25 + random.nextDouble() * 0.25, random.nextGaussian() * 0.12));
                }
            }
            for (int k = 0; k < 5; k++) {
                double s = (random.nextDouble() * 2.0 - 1.0) * half;
                emit(level, smoke(0xF0FBFF, 0.9F, 26).gravity(-0.004F).drag(0.92F), c.add(this.side.scale(s)).add(0.0, 0.6, 0.0), this.dir.scale(0.08));
            }
        }
    }
}
