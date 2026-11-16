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
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.yezon.theabyss.TheabyssMod;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class CodexMultiblock {
    private static final BlockDisplayContext CONTEXT = BlockDisplayContext.create();
    private static final Map<CodexBook.Page, Structure> CACHE = new WeakHashMap<>();
    private static final int FULL_BRIGHT = 15728880;

    private CodexMultiblock() {
    }

    record Placed(BlockPos pos, BlockState state) {
    }

    record Structure(List<Placed> blocks, int sizeX, int sizeY, int sizeZ) {
    }

    public record State(Structure structure, float yaw, float pixelsPerBlock, int x0, int y0, int x1, int y1, @Nullable ScreenRectangle scissorArea,
                        @Nullable ScreenRectangle bounds) implements PictureInPictureRenderState {
        @Override
        public float scale() {
            return this.pixelsPerBlock;
        }
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
                    BlockState state = BuiltInRegistries.BLOCK.getValue(Identifier.parse(id.getAsString())).defaultBlockState();
                    if (!state.isAir()) {
                        blocks.add(new Placed(new BlockPos(x, y, z), state));
                    }
                }
            }
        }
        return new Structure(blocks, sizeX, Math.max(1, layers.size()), sizeZ);
    }

    static void submit(net.minecraft.client.gui.GuiGraphicsExtractor g, Structure structure, int x0, int y0, int x1, int y1) {
        int size = Math.min(x1 - x0, y1 - y0);
        double diagonal = Math.sqrt(structure.sizeX() * structure.sizeX() + structure.sizeZ() * structure.sizeZ());
        float pixels = (float)Math.min(size * 0.9 / diagonal, size * 0.9 / (structure.sizeY() + diagonal * 0.5));
        float yaw = (float)((System.nanoTime() / 1.0E9 * 18.0) % 360.0) + 45.0F;
        g.submitPictureInPictureRenderState(new State(structure, yaw, pixels, x0, y0, x1, y1, null,
            PictureInPictureRenderState.getBounds(x0, y0, x1, y1, null)));
    }

    @SubscribeEvent
    public static void register(RegisterPictureInPictureRenderersEvent event) {
        event.register(State.class, Renderer::new);
    }

    private static final class Renderer extends PictureInPictureRenderer<State> {
        @Override
        public Class<State> getRenderStateClass() {
            return State.class;
        }

        @Override
        protected void renderToTexture(State state, PoseStack poseStack, SubmitNodeCollector collector) {
            Minecraft mc = Minecraft.getInstance();
            mc.gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_3D);
            Structure s = state.structure();
            float height = (state.y1() - state.y0()) / state.scale();
            poseStack.pushPose();
            poseStack.translate(0.0F, -height / 2.0F, 0.0F);
            poseStack.rotateDegrees(Axis.ZP, 180.0F);
            poseStack.rotateDegrees(Axis.XP, -30.0F);
            poseStack.rotateDegrees(Axis.YP, state.yaw());
            poseStack.translate(-s.sizeX() / 2.0F, -s.sizeY() / 2.0F, -s.sizeZ() / 2.0F);
            for (Placed placed : s.blocks()) {
                BlockModelRenderState model = new BlockModelRenderState();
                mc.getBlockModelResolver().update(model, placed.state(), CONTEXT);
                poseStack.pushPose();
                poseStack.translate(placed.pos().getX(), placed.pos().getY(), placed.pos().getZ());
                model.submit(poseStack, collector, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
            poseStack.popPose();
        }

        @Override
        protected String getTextureLabel() {
            return "theabyss codex multiblock";
        }
    }
}
