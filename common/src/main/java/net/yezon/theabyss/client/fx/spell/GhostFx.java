package net.yezon.theabyss.client.fx.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.compat.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;

final class GhostFx extends Effect {
    private static PlayerModel<LivingEntity> wide;
    private static PlayerModel<LivingEntity> slim;
    private static ModelPart wideRoot;
    private static ModelPart slimRoot;

    private record Ghost(Vec3 at, float yaw) {
    }

    private final int colour;
    private final List<Ghost> ghosts = new ArrayList<>();
    private final ResourceLocation skin;
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
        this.skin = skin.texture();
        this.slimArms = skin.model() == PlayerSkin.Model.SLIM;
    }

    static PlayerSkin skinOf(int id) {
        Minecraft mc = Minecraft.getInstance();
        Entity e = mc.level == null ? null : mc.level.getEntity(id);
        if (e == null) {
            return new PlayerSkin(DefaultPlayerSkin.getDefaultTexture(), null, null, null, PlayerSkin.Model.SLIM, true);
        }
        PlayerInfo info = mc.getConnection() != null ? mc.getConnection().getPlayerInfo(e.getUUID()) : null;
        return info != null ? info.getSkin() : DefaultPlayerSkin.get(e.getUUID());
    }

    static PlayerModel<LivingEntity> model(boolean slimArms) {
        if (wide == null) {
            var models = Minecraft.getInstance().getEntityModels();
            wideRoot = models.bakeLayer(ModelLayers.PLAYER);
            slimRoot = models.bakeLayer(ModelLayers.PLAYER_SLIM);
            wide = new PlayerModel<>(wideRoot, false);
            slim = new PlayerModel<>(slimRoot, true);
        }
        return slimArms ? slim : wide;
    }

    static void renderFigure(PlayerModel<LivingEntity> model, PoseStack ps, VertexConsumer buffer, int colour) {
        model.hat.copyFrom(model.head);
        model.jacket.copyFrom(model.body);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftPants.copyFrom(model.leftLeg);
        model.rightPants.copyFrom(model.rightLeg);
        model.renderToBuffer(ps, buffer, 15728880, OverlayTexture.NO_OVERLAY, colour);
    }

    static void resetPose(PlayerModel<LivingEntity> model) {
        ModelPart root = model == slim ? slimRoot : wideRoot;
        if (root != null) {
            root.getAllParts().forEach(ModelPart::resetPose);
        }
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
        PlayerModel<LivingEntity> model = model(this.slimArms);
        int tint = lighten(this.colour, 0.6F);
        int n = this.ghosts.size();
        List<Vec3> rel = new ArrayList<>();
        for (Ghost g : this.ghosts) {
            rel.add(f.rel(g.at));
        }
        f.mesh(RenderType.entityTranslucent(this.skin), (pose, buffer, camera) -> {
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
                ps.mulPose(Axis.YP.rotationDegrees(180.0F - g.yaw));
                ps.mulPose(Axis.XP.rotationDegrees(-12.0F));
                ps.scale(-0.9375F, -0.9375F, 0.9375F);
                ps.translate(0.0F, -1.501F, 0.0F);
                stride(model, i);
                renderFigure(model, ps, buffer, ARGB.color(alpha, tint));
                resetPose(model);
                ps.popPose();
            }
        });
    }

    private static void stride(PlayerModel<LivingEntity> model, int i) {
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
