package net.yezon.theabyss.client.codex;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.client.gui.GuiPicture;
import org.joml.Matrix4f;

public final class CodexMultiblock {
    private static final Map<CodexBook.Page, Structure> CACHE = new WeakHashMap<>();
    private static final int FULL_BRIGHT = 15728880;
    private static final GuiPicture PICTURE = new GuiPicture();

    private CodexMultiblock() {
    }

    record Placed(BlockPos pos, BlockState state) {
    }

    record Structure(List<Placed> blocks, int sizeX, int sizeY, int sizeZ) {
    }

    static Structure structure(CodexBook.Page page) {
        return CACHE.computeIfAbsent(page, CodexMultiblock::parse);
    }

    private static Structure parse(CodexBook.Page page) {
        JsonObject json = page.json();
        List<Placed> blocks = new ArrayList<>();
        int sizeX = 1;
        int sizeZ = 1;
        JsonArray layers = json.has("pattern") ? json.getAsJsonArray("pattern") : new JsonArray();
        JsonObject mapping = json.has("mapping") ? json.getAsJsonObject("mapping") : new JsonObject();
        for (int y = 0; y < layers.size(); y++) {
            JsonArray rows = layers.get(y).getAsJsonArray();
            sizeZ = Math.max(sizeZ, rows.size());
            for (int z = 0; z < rows.size(); z++) {
                String row = rows.get(z).getAsString();
                sizeX = Math.max(sizeX, row.length());
                for (int x = 0; x < row.length(); x++) {
                    String key = String.valueOf(row.charAt(x));
                    JsonElement id = mapping.get(key);
                    if (id == null) {
                        continue;
                    }
                    BlockState state = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id.getAsString())).defaultBlockState();
                    if (!state.isAir()) {
                        blocks.add(new Placed(new BlockPos(x, y, z), state));
                    }
                }
            }
        }
        return new Structure(blocks, sizeX, Math.max(1, layers.size()), sizeZ);
    }

    static void render(GuiGraphics g, Structure structure, int x0, int y0, int x1, int y1) {
        int size = Math.min(x1 - x0, y1 - y0);
        double diagonal = Math.sqrt(structure.sizeX() * structure.sizeX() + structure.sizeZ() * structure.sizeZ());
        float pixels = (float)Math.min(size * 0.9 / diagonal, size * 0.9 / (structure.sizeY() + diagonal * 0.5));
        float yaw = (float)((System.nanoTime() / 1.0E9 * 18.0) % 360.0) + 45.0F;
        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher blocks = mc.getBlockRenderer();
        int guiScale = GuiPicture.guiScale();
        int width = (x1 - x0) * guiScale;
        int height = (y1 - y0) * guiScale;
        MultiBufferSource.BufferSource buffers = PICTURE.begin(g, width, height);
        MultiBufferSource blockBuffers = type -> buffers.getBuffer(itemSheet(type));
        Lighting.setupFor3DItems();
        PoseStack poseStack = new PoseStack();
        poseStack.translate(width / 2.0F, height, 0.0F);
        float scale = guiScale * pixels;
        poseStack.scale(scale, scale, -scale);
        float blocksHigh = (y1 - y0) / pixels;
        poseStack.translate(0.0F, -blocksHigh / 2.0F, 0.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.translate(-structure.sizeX() / 2.0F, -structure.sizeY() / 2.0F, -structure.sizeZ() / 2.0F);
        for (Placed placed : structure.blocks()) {
            poseStack.pushPose();
            poseStack.translate(placed.pos().getX(), placed.pos().getY(), placed.pos().getZ());
            blocks.renderSingleBlock(placed.state(), poseStack, blockBuffers, FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        PICTURE.end();
        PICTURE.blit(new Matrix4f(), x0, y0, x1, y1);
    }

    private static RenderType itemSheet(RenderType type) {
        if (type == Sheets.translucentItemSheet()) {
            return Sheets.translucentCullBlockSheet();
        }
        if (type == Sheets.solidBlockSheet()) {
            return Sheets.cutoutBlockSheet();
        }
        return type;
    }
}
