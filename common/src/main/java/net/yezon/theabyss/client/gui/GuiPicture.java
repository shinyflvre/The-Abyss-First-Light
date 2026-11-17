package net.yezon.theabyss.client.gui;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public final class GuiPicture {
    private static final GuiPicture ITEMS = new GuiPicture();
    private static final int FULL_BRIGHT = 15728880;
    private TextureTarget target;
    private Matrix4f projection;
    private VertexSorting sorting;

    public static int guiScale() {
        return (int)Minecraft.getInstance().getWindow().getGuiScale();
    }

    public MultiBufferSource.BufferSource begin(GuiGraphics g, int width, int height) {
        g.flush();
        if (this.target == null) {
            this.target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
        } else if (this.target.width != width || this.target.height != height) {
            this.target.resize(width, height, Minecraft.ON_OSX);
        }
        this.target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.target.clear(Minecraft.ON_OSX);
        this.target.bindWrite(true);
        this.projection = new Matrix4f(RenderSystem.getProjectionMatrix());
        this.sorting = RenderSystem.getVertexSorting();
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0F, width, height, 0.0F, -1000.0F, 1000.0F), VertexSorting.ORTHOGRAPHIC_Z);
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.identity();
        RenderSystem.applyModelViewMatrix();
        return Minecraft.getInstance().renderBuffers().bufferSource();
    }

    public void end() {
        Minecraft mc = Minecraft.getInstance();
        mc.renderBuffers().bufferSource().endBatch();
        RenderSystem.getModelViewStack().popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(this.projection, this.sorting);
        Lighting.setupFor3DItems();
        mc.getMainRenderTarget().bindWrite(true);
    }

    public void blit(Matrix4f matrix, float x0, float y0, float x1, float y1) {
        RenderSystem.setShaderTexture(0, this.target.getColorTextureId());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, x0, y0, 0.0F).setUv(0.0F, 1.0F);
        buffer.addVertex(matrix, x0, y1, 0.0F).setUv(0.0F, 0.0F);
        buffer.addVertex(matrix, x1, y1, 0.0F).setUv(1.0F, 0.0F);
        buffer.addVertex(matrix, x1, y0, 0.0F).setUv(1.0F, 1.0F);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void item(GuiGraphics g, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getItemRenderer().getModel(stack, mc.level, mc.player, 0);
        int size = 16 * guiScale();
        MultiBufferSource.BufferSource buffers = ITEMS.begin(g, size, size);
        PoseStack poseStack = new PoseStack();
        poseStack.translate(size / 2.0F, size / 2.0F, 0.0F);
        poseStack.scale(size, -size, size);
        if (model.usesBlockLight()) {
            Lighting.setupFor3DItems();
        } else {
            Lighting.setupForFlatItems();
        }
        mc.getItemRenderer().render(stack, ItemDisplayContext.GUI, false, poseStack, buffers, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
        ITEMS.end();
        ITEMS.blit(g.pose().last().pose(), x, y, x + 16, y + 16);
    }
}
