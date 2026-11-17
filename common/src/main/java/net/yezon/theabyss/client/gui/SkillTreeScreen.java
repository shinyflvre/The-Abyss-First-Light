package net.yezon.theabyss.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.yezon.theabyss.compat.ARGB;
import net.yezon.theabyss.compat.GuiGraphicsCompat;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.platform.client.event.RegisterShadersEvent;
import net.yezon.theabyss.platform.client.network.ClientPacketDistributor;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.skill.SkillClient;
import net.yezon.theabyss.network.SkillRequestMessage;
import net.yezon.theabyss.skill.Skill;
import org.joml.Matrix4f;

public class SkillTreeScreen extends Screen {
    public static final ResourceLocation SHADER = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "skill_tree");
    private static ShaderInstance shader;
    private static final ResourceLocation FRAMES = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "textures/gui/spell_wheel_frames.png");
    private static final int LOCKED = 0;
    private static final int OPEN = 1;
    private static final int LEARNED = 2;
    private static final int NODE = 22;
    private static final int KEYSTONE = 28;
    private static final float TREE_SHIFT = 26.0F;
    private static final float TREE_HEIGHT = 196.0F;
    private static final long FORGET_CONFIRM_MS = 4000L;

    private record Node(Skill skill, float x, float y, ResourceLocation icon) {
        boolean keystone() {
            return this.skill.keystone();
        }
    }

    private final List<Node> nodes = new ArrayList<>();
    private final long openedAt = Util.getMillis();
    private int hovered = -1;
    private long forgetArmed;

    public SkillTreeScreen() {
        super(Component.translatable("skill.theabyss.title"));
        for (Skill skill : Skill.values()) {
            float x = 0.0F;
            float y = 0.0F;
            if (skill.branch() >= 0) {
                double angle = Math.toRadians(120.0 * skill.branch());
                float dx = (float)Math.sin(angle);
                float dy = (float)-Math.cos(angle);
                float px = (float)Math.cos(angle);
                float py = (float)Math.sin(angle);
                float side = skill.slot() == 2 ? -22.0F : skill.slot() == 3 ? 22.0F : 0.0F;
                float reach = switch (skill.slot()) {
                    case 1 -> 32.0F;
                    case 2, 3 -> 58.0F;
                    case 4 -> 82.0F;
                    default -> 108.0F;
                };
                x = dx * reach + px * side;
                y = dy * reach + py * side;
            }
            this.nodes.add(new Node(skill, x, y, ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "textures/gui/skill/" + skill.id() + ".png")));
        }
    }

    public static void registerShader(RegisterShadersEvent event) {
        event.registerShader(SHADER, DefaultVertexFormat.POSITION_TEX_COLOR, loaded -> shader = loaded);
    }

    private int state(Node node) {
        return SkillClient.has(node.skill()) ? LEARNED : SkillClient.available(node.skill()) ? OPEN : LOCKED;
    }

    private boolean forgetArmed() {
        return Util.getMillis() - this.forgetArmed < FORGET_CONFIRM_MS;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    private float fit() {
        return Math.min(1.0F, (this.height - 64.0F) / TREE_HEIGHT);
    }

    private float centreX() {
        return this.width / 2.0F;
    }

    private float centreY() {
        return this.height / 2.0F + 6.0F + TREE_SHIFT * this.fit();
    }

    private int size(Node node) {
        return node.keystone() ? KEYSTONE : NODE;
    }

    private int nodeAt(double mouseX, double mouseY) {
        float fit = this.fit();
        double tx = (mouseX - this.centreX()) / fit;
        double ty = (mouseY - this.centreY()) / fit;
        for (int i = 0; i < this.nodes.size(); i++) {
            Node node = this.nodes.get(i);
            double half = this.size(node) / 2.0 + 1.0;
            if (Math.abs(tx - node.x()) <= half && Math.abs(ty - node.y()) <= half) {
                return i;
            }
        }
        return -1;
    }

    private static void effect(GuiGraphics g, int x, int y, int width, int height, int color) {
        ShaderInstance program = shader;
        if (program == null || color >>> 24 == 0) {
            return;
        }
        RenderSystem.setShaderTexture(0, FRAMES);
        RenderSystem.setShader(() -> program);
        GuiGraphicsCompat.beginTranslucent();
        Matrix4f matrix = g.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.addVertex(matrix, x, y, 0.0F).setUv(0.0F, 0.0F).setColor(color);
        buffer.addVertex(matrix, x, y + height, 0.0F).setUv(0.0F, 1.0F).setColor(color);
        buffer.addVertex(matrix, x + width, y + height, 0.0F).setUv(1.0F, 1.0F).setColor(color);
        buffer.addVertex(matrix, x + width, y, 0.0F).setUv(1.0F, 0.0F).setColor(color);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        GuiGraphicsCompat.endTranslucent();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        float fade = Mth.clamp((Util.getMillis() - this.openedAt) / 250.0F, 0.0F, 1.0F);
        int alpha = Math.round(255 * fade);
        float aspect = this.width / (float)Math.max(1, this.height);
        float fit = this.fit();
        float cx = this.centreX();
        float cy = this.centreY();
        this.hovered = this.nodeAt(mouseX, mouseY);
        effect(g, 0, 0, this.width, this.height, ARGB.color(25, Math.round(aspect / 4.0F * 255), Math.round(cy / this.height * 255), alpha));
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0.0F);
        g.pose().scale(fit, fit, 1.0F);
        for (int i = 0; i < this.nodes.size(); i++) {
            Node node = this.nodes.get(i);
            int nodeState = this.state(node);
            for (Skill parentSkill : node.skill().parents()) {
                int p = parentSkill.ordinal();
                Node parent = this.nodes.get(p);
                float length = (float)Math.hypot(node.x() - parent.x(), node.y() - parent.y());
                float linkState = nodeState == LEARNED ? 1.0F : nodeState == OPEN && SkillClient.has(parentSkill) ? 0.5F : 0.0F;
                g.pose().pushPose();
                g.pose().translate(parent.x(), parent.y(), 0.0F);
                g.pose().mulPose(Axis.ZP.rotation((float)Math.atan2(node.y() - parent.y(), node.x() - parent.x())));
                effect(g, 0, -4, Math.round(length), 8, ARGB.color(89, Math.round(linkState * 255), (i * 37 + p * 11) % 255, Math.round(Math.min(255.0F, length))));
                g.pose().popPose();
            }
        }
        for (int i = 0; i < this.nodes.size(); i++) {
            Node node = this.nodes.get(i);
            int halo = node.keystone() ? 60 : 44;
            int nodeState = this.state(node);
            float haloState = nodeState == LEARNED ? 1.0F : nodeState == OPEN ? 0.5F : 0.0F;
            effect(g, Math.round(node.x() - halo / 2.0F), Math.round(node.y() - halo / 2.0F), halo, halo,
                ARGB.color(153, Math.round(haloState * 255), i == this.hovered ? 255 : 0, node.keystone() ? 255 : 0));
        }
        for (int i = 0; i < this.nodes.size(); i++) {
            Node node = this.nodes.get(i);
            int s = this.size(node);
            int nodeState = this.state(node);
            int tint = nodeState == LOCKED ? ARGB.color(alpha, 95, 110, 125) : ARGB.color(alpha, 255, 255, 255);
            float u = i == this.hovered ? 32.0F : 0.0F;
            GuiGraphicsCompat.blit(g, FRAMES, Math.round(node.x() - s / 2.0F), Math.round(node.y() - s / 2.0F), u, 0.0F, s, s, 32, 32, 64, 32, tint);
            int glyph = nodeState == LEARNED ? ARGB.color(alpha, 255, 255, 255) : nodeState == OPEN ? ARGB.color(Math.round(alpha * 0.8F), 170, 200, 205)
                : ARGB.color(Math.round(alpha * 0.55F), 70, 84, 96);
            GuiGraphicsCompat.blit(g, node.icon(), Math.round(node.x()) - 8, Math.round(node.y()) - 8, 0.0F, 0.0F, 16, 16, 16, 16, 16, 16, glyph);
        }
        g.pose().popPose();
        int pale = ARGB.color(alpha, 0xB4, 0xFD, 0xEA);
        GuiGraphicsCompat.centeredText(g, this.font, this.title, this.width / 2, 10, pale);
        GuiGraphicsCompat.centeredText(g, this.font, Component.translatable("skill.theabyss.subtitle").withStyle(ChatFormatting.GRAY), this.width / 2, 21,
            ARGB.color(alpha, 255, 255, 255));
        int levels = mc.player != null ? mc.player.experienceLevel : 0;
        GuiGraphicsCompat.centeredText(g, this.font, Component.translatable("skill.theabyss.levels", levels), this.width / 2, this.height - 14,
            ARGB.color(alpha, 0x80, 0xFF, 0x20));
        if (this.hovered >= 0) {
            this.info(g, this.hovered, mouseX, mouseY, alpha, levels);
        }
    }

    private void info(GuiGraphics g, int index, int mouseX, int mouseY, int alpha, int levels) {
        Node node = this.nodes.get(index);
        Skill skill = node.skill();
        List<FormattedCharSequence> lines = new ArrayList<>();
        int colour = node.keystone() ? 0xC9A2FF : 0x7EF5E4;
        lines.add(Component.translatable("skill.theabyss." + skill.id()).withStyle(style -> style.withColor(colour)).getVisualOrderText());
        if (skill.branch() >= 0) {
            lines.add(Component.translatable("skill.theabyss.branch." + Skill.BRANCHES[skill.branch()]).withStyle(ChatFormatting.DARK_AQUA).getVisualOrderText());
        }
        lines.addAll(this.font.split(Component.translatable("skill.theabyss." + skill.id() + ".desc").withStyle(ChatFormatting.GRAY), 160));
        int nodeState = this.state(node);
        Component status;
        if (skill == Skill.REFLECTION) {
            int refund = SkillClient.spent() / 2;
            status = SkillClient.spent() <= 0 ? null
                : this.forgetArmed() ? Component.translatable("skill.theabyss.forget_confirm").withStyle(ChatFormatting.RED)
                : Component.translatable("skill.theabyss.forget", refund).withStyle(ChatFormatting.GOLD);
        } else {
            status = nodeState == LEARNED ? Component.translatable("skill.theabyss.learned").withStyle(ChatFormatting.AQUA)
                : nodeState == OPEN ? Component.translatable("skill.theabyss.cost", skill.cost())
                    .withStyle(levels >= skill.cost() ? ChatFormatting.GREEN : ChatFormatting.RED)
                : Component.translatable("skill.theabyss.locked").withStyle(ChatFormatting.DARK_GRAY);
        }
        if (status != null) {
            lines.add(status.getVisualOrderText());
        }
        int w = 0;
        for (FormattedCharSequence line : lines) {
            w = Math.max(w, this.font.width(line));
        }
        int h = lines.size() * 10 + 6;
        int bx = mouseX + 12 + w + 8 > this.width ? mouseX - 12 - w - 8 : mouseX + 12;
        int by = Mth.clamp(mouseY - h / 2, 2, this.height - h - 2);
        g.fill(bx, by, bx + w + 8, by + h, ARGB.color(Math.round(alpha * 0.88F), 6, 14, 20));
        g.renderOutline(bx, by, w + 8, h, ARGB.color(alpha / 2, colour));
        int ty = by + 4;
        for (FormattedCharSequence line : lines) {
            GuiGraphicsCompat.text(g, this.font, line, bx + 4, ty, ARGB.color(alpha, 255, 255, 255), true);
            ty += 10;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            int index = this.nodeAt(mouseX, mouseY);
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (index < 0 || player == null) {
                return true;
            }
            Node node = this.nodes.get(index);
            int nodeState = this.state(node);
            if (node.skill() == Skill.REFLECTION) {
                if (SkillClient.spent() <= 0) {
                    return true;
                }
                if (this.forgetArmed()) {
                    this.forgetArmed = 0L;
                    ClientPacketDistributor.sendToServer(new SkillRequestMessage(SkillRequestMessage.FORGET, node.skill().id()));
                } else {
                    this.forgetArmed = Util.getMillis();
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.SCULK_CLICKING, 0.6F, 0.8F));
                }
            } else if (nodeState == OPEN && player.experienceLevel >= node.skill().cost()) {
                ClientPacketDistributor.sendToServer(new SkillRequestMessage(SkillRequestMessage.LEARN, node.skill().id()));
            } else if (nodeState != LEARNED) {
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.SCULK_CLICKING, 0.5F, 0.6F));
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
