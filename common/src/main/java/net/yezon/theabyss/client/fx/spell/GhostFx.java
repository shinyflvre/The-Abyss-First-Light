package net.yezon.theabyss.client.fx.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;

final class GhostFx extends Effect {
    private static HumanoidModel<HumanoidRenderState> wide;
    private static HumanoidModel<HumanoidRenderState> slim;

    private record Ghost(Vec3 at, float yaw) {
    }

    private final int colour;
    private final List<Ghost> ghosts = new ArrayList<>();
    private final Identifier skin;
    private final boolean slimArms;
    private final int life;

    GhostFx(SpellFxMessage m) {
        this.colour = m.data().get(0).intValue();
        int n = (m.data().size() - 1) / 4;
        int count = n == 1 ? 1 : n - 1;
        for (int i = 0; i < count; i++) {
            int k = 1 + i * 4;
            this.ghosts.add(new Ghost(new Vec3(m.data().get(k), m.data().get(k + 1), m.data().get(k + 2)), m.data().get(k + 3).floatValue()));
        }
        this.life = m.ticks();
        PlayerSkin skin = skinOf(m.ids().isEmpty() ? -1 : m.ids().get(0));
        this.skin = skin.body().texturePath();
        this.slimArms = skin.model() == PlayerModelType.SLIM;
    }

    static PlayerSkin skinOf(int id) {
        Minecraft mc = Minecraft.getInstance();
        Entity e = mc.level == null ? null : mc.level.getEntity(id);
        if (e == null) {
            return DefaultPlayerSkin.getDefaultSkin();
        }
        PlayerInfo info = mc.getConnection() != null ? mc.getConnection().getPlayerInfo(e.getUUID()) : null;
        return info != null ? info.getSkin() : DefaultPlayerSkin.get(e.getUUID());
    }

    static HumanoidModel<HumanoidRenderState> model(boolean slimArms) {
        if (wide == null) {
            var models = Minecraft.getInstance().getEntityModels();
            wide = new HumanoidModel<>(models.bakeLayer(ModelLayers.PLAYER));
            slim = new HumanoidModel<>(models.bakeLayer(ModelLayers.PLAYER_SLIM));
        }
        return slimArms ? slim : wide;
    }

    @Override
    public float life() {
        return this.life;
    }

    @Override
    public void draw(Frame f) {
        float fade = this.fade(0.0F, this.life * 0.8F);
        if (fade <= 0.02F || this.ghosts.isEmpty()) {
            return;
        }
        HumanoidModel<HumanoidRenderState> model = model(this.slimArms);
        int tint = lighten(this.colour, 0.6F);
        int n = this.ghosts.size();
        List<Vec3> rel = new ArrayList<>();
        for (Ghost g : this.ghosts) {
            rel.add(f.rel(g.at));
        }
        f.mesh(RenderTypes.entityTranslucent(this.skin), (pose, buffer, camera) -> {
            PoseStack ps = new PoseStack();
            ps.last().pose().set(pose.pose());
            ps.last().normal().set(pose.normal());
            for (int i = 0; i < n; i++) {
                Ghost g = this.ghosts.get(i);
                float age = n == 1 ? 1.0F : 0.35F + 0.65F * i / (n - 1);
                Vec3 at = rel.get(i);
                float near = (float)Math.max(0.0, Math.min(1.0, (at.add(0.0, 1.0, 0.0).length() - 1.0) / 1.0));
                int alpha = Math.round(140 * fade * age * near);
                if (alpha < 4) {
                    continue;
                }
                ps.pushPose();
                ps.translate(at.x, at.y, at.z);
                ps.rotateDegrees(Axis.YP, 180.0F - g.yaw);
                ps.rotateDegrees(Axis.XP, -12.0F);
                ps.scale(-0.9375F, -0.9375F, 0.9375F);
                ps.translate(0.0F, -1.501F, 0.0F);
                stride(model, i);
                model.root().render(ps, buffer, 15728880, OverlayTexture.NO_OVERLAY, ARGB.color(alpha, tint));
                model.root().getAllParts().forEach(part -> part.resetPose());
                ps.popPose();
            }
        });
    }

    private static void stride(HumanoidModel<HumanoidRenderState> model, int i) {
        float s = i % 2 == 0 ? 1.0F : -1.0F;
        model.rightArm.xRot = 0.9F * s;
        model.leftArm.xRot = -0.9F * s;
        model.rightLeg.xRot = -0.8F * s;
        model.leftLeg.xRot = 0.8F * s;
    }

    private static int lighten(int colour, float amount) {
        int r = (colour >> 16) & 255;
        int g = (colour >> 8) & 255;
        int b = colour & 255;
        r += (int)((255 - r) * amount);
        g += (int)((255 - g) * amount);
        b += (int)((255 - b) * amount);
        return r << 16 | g << 8 | b;
    }
}
