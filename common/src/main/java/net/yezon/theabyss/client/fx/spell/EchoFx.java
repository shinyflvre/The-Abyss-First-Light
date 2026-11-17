package net.yezon.theabyss.client.fx.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.compat.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;

final class EchoFx {
    static final int LILAC = 0xB47CFF;
    static final int PEARL = 0xE8E2FB;

    private record Figure(Vec3 at, float yaw, int alpha, int tint) {
    }

    private EchoFx() {
    }

    private static void figures(Frame f, ResourceLocation skin, boolean slim, List<Figure> figures) {
        if (figures.isEmpty()) {
            return;
        }
        PlayerModel<LivingEntity> model = GhostFx.model(slim);
        List<Vec3> rel = new ArrayList<>();
        for (Figure figure : figures) {
            rel.add(f.rel(figure.at));
        }
        f.mesh(RenderType.entityTranslucent(skin), (pose, buffer, camera) -> {
            PoseStack ps = new PoseStack();
            ps.last().pose().set(pose.pose());
            ps.last().normal().set(pose.normal());
            for (int i = 0; i < figures.size(); i++) {
                Figure figure = figures.get(i);
                Vec3 at = rel.get(i);
                float near = (float)Math.max(0.0, Math.min(1.0, (at.add(0.0, 1.0, 0.0).length() - 1.0) / 1.0));
                int alpha = Math.round(figure.alpha * near);
                if (alpha < 4) {
                    continue;
                }
                ps.pushPose();
                ps.translate(at.x, at.y, at.z);
                ps.mulPose(Axis.YP.rotationDegrees(180.0F - figure.yaw));
                ps.scale(-0.9375F, -0.9375F, 0.9375F);
                ps.translate(0.0F, -1.501F, 0.0F);
                GhostFx.renderFigure(model, ps, buffer, ARGB.color(alpha, figure.tint));
                ps.popPose();
            }
        });
    }

    private static void motes(ClientLevel level, Vec3 at, int count, double spread) {
        RandomSource random = level.getRandom();
        for (int k = 0; k < count; k++) {
            Vec3 spot = at.add(random.nextGaussian() * spread, random.nextDouble() * 1.9, random.nextGaussian() * spread);
            Effect.emit(level, Effect.sparkle(k % 2 == 0 ? PEARL : LILAC, 0.1F, 30).gravity(-0.003F), spot, new Vec3(0.0, 0.015, 0.0));
        }
    }

    static final class Summon extends Effect {
        private final Vec3 to;
        private final float yaw;
        private final int life;
        private final ResourceLocation skin;
        private final boolean slim;
        private boolean burst;

        Summon(SpellFxMessage m) {
            this.to = SpellFx.vec(m, 3);
            this.yaw = m.data().get(6).floatValue();
            this.life = m.ticks();
            PlayerSkin skin = GhostFx.skinOf(m.ids().isEmpty() ? -1 : m.ids().get(0));
            this.skin = skin.texture();
            this.slim = skin.model() == PlayerSkin.Model.SLIM;
        }

        @Override
        public float life() {
            return this.life;
        }

        @Override
        public void draw(Frame f) {
            float age = this.age();
            float k = this.fade(2.0F, this.life * 0.45F);
            double gather = smooth(age / (this.life * 0.7));
            List<Figure> figures = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                float angle = this.yaw + 45.0F + i * 90.0F;
                Vec3 out = Vec3.directionFromRotation(0.0F, angle).scale(2.6 * (1.0 - gather));
                int alpha = Math.round(130 * k * (float)(0.4 + 0.6 * gather) * (float)(1.0 - gather * 0.85));
                figures.add(new Figure(this.to.add(out), this.yaw, alpha, i % 2 == 0 ? PEARL : LILAC));
            }
            figures(f, this.skin, this.slim, figures);
            if (gather > 0.85) {
                f.flare(this.to.add(0.0, 1.1, 0.0), (float)(2.6 * (gather - 0.85) / 0.15) * k, PEARL, 0.8F * k);
            }
        }

        @Override
        public void tick(ClientLevel level) {
            if (!this.burst && this.age() > this.life * 0.65F) {
                this.burst = true;
                motes(level, this.to, 22, 0.35);
            }
        }
    }

    static final class Dissolve extends Effect {
        private final Vec3 at;
        private final float yaw;
        private final int life;
        private final ResourceLocation skin;
        private final boolean slim;
        private boolean burst;

        Dissolve(SpellFxMessage m) {
            this.at = SpellFx.vec(m, 0);
            this.yaw = m.data().get(3).floatValue();
            this.life = m.ticks();
            PlayerSkin skin = GhostFx.skinOf(m.ids().isEmpty() ? -1 : m.ids().get(0));
            this.skin = skin.texture();
            this.slim = skin.model() == PlayerSkin.Model.SLIM;
        }

        @Override
        public float life() {
            return this.life;
        }

        @Override
        public void draw(Frame f) {
            float age = this.age();
            float k = this.fade(0.0F, this.life * 0.9F);
            double t = easeOut(age / this.life);
            List<Figure> figures = new ArrayList<>();
            figures.add(new Figure(this.at.add(0.0, 0.9 * t, 0.0), this.yaw, Math.round(150 * k * k), PEARL));
            for (int i = 0; i < 3; i++) {
                Vec3 out = Vec3.directionFromRotation(0.0F, this.yaw + 90.0F + i * 90.0F).scale(1.6 * t);
                figures.add(new Figure(this.at.add(out).add(0.0, 0.4 * t, 0.0), this.yaw, Math.round(90 * k * k * k), LILAC));
            }
            figures(f, this.skin, this.slim, figures);
            Vec3 side = new Vec3(1.0, 0.0, 0.0);
            Vec3 depth = new Vec3(0.0, 0.0, 1.0);
            for (int i = 0; i < 3; i++) {
                double phase = (age - i * 4.0) / (this.life * 0.7);
                if (phase <= 0.0 || phase >= 1.0) {
                    continue;
                }
                double r = 0.5 + 4.0 * easeOut(phase);
                float light = (float)(1.0 - phase) * k;
                f.circle(this.at.add(0.0, 1.0 + i * 0.25, 0.0), side, depth, r, 0.08F * light, light, i == 1 ? PEARL : LILAC);
            }
        }

        @Override
        public void tick(ClientLevel level) {
            if (!this.burst) {
                this.burst = true;
                motes(level, this.at, 28, 0.3);
            }
        }
    }

    static final class Link extends Effect {
        private final Vec3 from;
        private final Vec3 to;
        private final int life;

        Link(SpellFxMessage m) {
            this.from = SpellFx.vec(m, 0);
            this.to = SpellFx.vec(m, 3);
            this.life = m.ticks();
        }

        @Override
        public float life() {
            return this.life;
        }

        @Override
        public void draw(Frame f) {
            float age = this.age();
            float k = this.fade(0.0F, this.life * 0.6F);
            Vec3 mid = this.from.lerp(this.to, 0.5).add(0.0, Math.min(4.0, this.from.distanceTo(this.to) * 0.1), 0.0);
            Vec3[] curve = Frame.curve(this.from, mid, this.to, 24);
            f.ribbon(curve, 0.3F * k, 0.45F * k, LILAC);
            f.ribbon(curve, 0.07F * k, k, PEARL);
            double head = smooth(age / (this.life * 0.55));
            int index = (int)Math.min(curve.length - 1, Math.round(head * (curve.length - 1)));
            f.flare(curve[index], 1.6F * k, PEARL, 0.9F * k);
            Vec3 side = new Vec3(1.0, 0.0, 0.0);
            Vec3 depth = new Vec3(0.0, 0.0, 1.0);
            double open = easeOut(age / 6.0);
            f.circle(this.from.add(0.0, -0.9, 0.0), side, depth, 0.9 * open, 0.08F * k, k, LILAC);
            f.circle(this.to.add(0.0, -0.9, 0.0), side, depth, 0.9 * open, 0.08F * k, k, PEARL);
        }
    }
}
