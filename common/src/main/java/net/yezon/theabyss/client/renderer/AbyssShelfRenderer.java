package net.yezon.theabyss.client.renderer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.HashCommon;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.block.wood.AbyssShelfBlock;
import net.yezon.theabyss.block.wood.AbyssShelfBlockEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public class AbyssShelfRenderer implements BlockEntityRenderer<AbyssShelfBlockEntity> {
    private static final float ITEM_SIZE = 0.25F;
    private static final float ALIGN_ITEMS_TO_BOTTOM = -0.25F;
    private static final Direction[] QUAD_SIDES = {null, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final Map<ResourceLocation, ShelfTransform> KNOWN = new HashMap<>();
    private final ItemRenderer itemRenderer;
    private final Map<Item, ShelfTransform> transforms = new HashMap<>();
    private final Map<BakedModel, Map<ShelfTransform, AABB>> boxes = new HashMap<>();

    static {
        ShelfTransform block = new ShelfTransform(new Vector3f(0.0F, 180.0F, 0.0F), new Vector3f(), new Vector3f(1.0F, 1.0F, 1.0F));
        ShelfTransform sideways = new ShelfTransform(new Vector3f(0.0F, 90.0F, 0.0F), new Vector3f(), new Vector3f(1.0F, 1.0F, 1.0F));
        ShelfTransform shelf = new ShelfTransform(new Vector3f(0.0F, 180.0F, 0.0F), new Vector3f(0.0F, 0.0F, 4.0F / 16.0F), new Vector3f(1.0F, 1.0F, 1.0F));
        KNOWN.put(ResourceLocation.withDefaultNamespace("block/block"), block);
        KNOWN.put(ResourceLocation.withDefaultNamespace("block/fence_inventory"), sideways);
        KNOWN.put(ResourceLocation.withDefaultNamespace("block/custom_fence_inventory"), sideways);
        KNOWN.put(ResourceLocation.withDefaultNamespace("block/wall_inventory"), sideways);
        KNOWN.put(ResourceLocation.withDefaultNamespace("block/template_anvil"), sideways);
        KNOWN.put(ResourceLocation.withDefaultNamespace("block/template_shelf_inventory"), shelf);
        KNOWN.put(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "block/template_shelf_inventory"), shelf);
        KNOWN.put(ResourceLocation.withDefaultNamespace("item/template_skull"),
            new ShelfTransform(new Vector3f(), new Vector3f(0.0F, 8.0F / 16.0F, 0.0F), new Vector3f(2.0F, 2.0F, 2.0F)));
        KNOWN.put(ResourceLocation.withDefaultNamespace("item/dragon_head"),
            new ShelfTransform(new Vector3f(), new Vector3f(0.0F, 2.0F / 16.0F, 0.0F), new Vector3f(1.25F, 1.25F, 1.25F)));
        KNOWN.put(ResourceLocation.withDefaultNamespace("item/shield"),
            new ShelfTransform(new Vector3f(), new Vector3f(11.0F / 16.0F, 18.5F / 16.0F, 8.7F / 16.0F), new Vector3f(1.4F, 1.4F, 1.4F)));
    }

    public AbyssShelfRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(AbyssShelfBlockEntity shelf, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        Direction facing = shelf.getBlockState().getValue(AbyssShelfBlock.FACING);
        float yRot = facing.getAxis().isHorizontal() ? -facing.toYRot() : 180.0F;
        boolean alignToBottom = shelf.getAlignItemsToBottom();
        NonNullList<ItemStack> items = shelf.getItems();
        int seed = HashCommon.long2int(shelf.getBlockPos().asLong());
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (!stack.isEmpty()) {
                this.renderItem(shelf, stack, poseStack, buffers, light, slot, seed + slot, yRot, alignToBottom);
            }
        }
    }

    private void renderItem(AbyssShelfBlockEntity shelf, ItemStack stack, PoseStack poseStack, MultiBufferSource buffers, int light, int slot, int seed, float yRot,
                            boolean alignToBottom) {
        BakedModel model = this.shelfModel(stack, shelf.getLevel(), seed);
        ShelfTransform transform = this.transforms.computeIfAbsent(stack.getItem(), AbyssShelfRenderer::lookup);
        AABB box = this.boxes.computeIfAbsent(model, m -> new HashMap<>())
            .computeIfAbsent(transform, t -> model.isCustomRenderer() ? this.drawnBounds(stack, model, t) : bounds(model, t));
        float itemSlotOffsetX = (slot - 1) * 0.3125F;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.translate(itemSlotOffsetX, alignToBottom ? ALIGN_ITEMS_TO_BOTTOM : 0.0F, -0.25F);
        poseStack.scale(ITEM_SIZE, ITEM_SIZE, ITEM_SIZE);
        double offsetY = -box.minY;
        if (!alignToBottom) {
            offsetY += -(box.maxY - box.minY) / 2.0;
        }
        poseStack.translate(0.0, offsetY, 0.0);
        transform.apply(poseStack);
        if (stack.is(Items.TRIDENT)) {
            renderQuads(stack, model, poseStack, buffers, light);
        } else {
            this.itemRenderer.render(stack, ItemDisplayContext.NONE, false, poseStack, buffers, light, OverlayTexture.NO_OVERLAY, model);
        }
        poseStack.popPose();
    }

    private static void renderQuads(ItemStack stack, BakedModel model, PoseStack poseStack, MultiBufferSource buffers, int light) {
        poseStack.pushPose();
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(buffers, ItemBlockRenderTypes.getRenderType(stack, true), true, stack.hasFoil());
        PoseStack.Pose pose = poseStack.last();
        RandomSource random = RandomSource.create();
        for (int i = 0; i <= Direction.values().length; i++) {
            random.setSeed(42L);
            for (BakedQuad quad : model.getQuads(null, i < Direction.values().length ? Direction.values()[i] : null, random)) {
                consumer.putBulkData(pose, quad, 1.0F, 1.0F, 1.0F, 1.0F, light, OverlayTexture.NO_OVERLAY);
            }
        }
        poseStack.popPose();
    }

    private BakedModel shelfModel(ItemStack stack, @Nullable Level level, int seed) {
        if (!stack.is(Items.TRIDENT) && !stack.is(Items.SPYGLASS)) {
            return this.itemRenderer.getModel(stack, level, null, seed);
        }
        ItemModelShaper shaper = this.itemRenderer.getItemModelShaper();
        BakedModel flat = shaper.getItemModel(stack);
        BakedModel resolved = flat.getOverrides().resolve(flat, stack, level instanceof ClientLevel client ? client : null, null, seed);
        return resolved == null ? shaper.getModelManager().getMissingModel() : resolved;
    }

    private AABB drawnBounds(ItemStack stack, BakedModel model, ShelfTransform transform) {
        PoseStack poseStack = new PoseStack();
        transform.apply(poseStack);
        ExtentCollector collector = new ExtentCollector();
        this.itemRenderer.render(stack, ItemDisplayContext.NONE, false, poseStack, collector, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
        return collector.box();
    }

    private static AABB bounds(BakedModel model, ShelfTransform transform) {
        Matrix4f matrix = transform.matrix();
        Vector3f point = new Vector3f();
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        int stride = DefaultVertexFormat.BLOCK.getVertexSize() / 4;
        boolean any = false;
        RandomSource random = RandomSource.create(42L);
        for (Direction side : QUAD_SIDES) {
            random.setSeed(42L);
            List<BakedQuad> quads = model.getQuads(null, side, random);
            for (BakedQuad quad : quads) {
                int[] vertices = quad.getVertices();
                for (int vertex = 0; vertex < 4; vertex++) {
                    int base = vertex * stride;
                    matrix.transformPosition(point.set(Float.intBitsToFloat(vertices[base]), Float.intBitsToFloat(vertices[base + 1]),
                        Float.intBitsToFloat(vertices[base + 2])));
                    minX = Math.min(minX, point.x);
                    minY = Math.min(minY, point.y);
                    minZ = Math.min(minZ, point.z);
                    maxX = Math.max(maxX, point.x);
                    maxY = Math.max(maxY, point.y);
                    maxZ = Math.max(maxZ, point.z);
                    any = true;
                }
            }
        }
        return any ? new AABB(minX, minY, minZ, maxX, maxY, maxZ) : new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }

    private static ShelfTransform lookup(Item item) {
        ResourceManager resources = Minecraft.getInstance().getResourceManager();
        ResourceLocation model = BuiltInRegistries.ITEM.getKey(item).withPrefix("item/");
        for (int depth = 0; depth < 32 && model != null; depth++) {
            ShelfTransform known = KNOWN.get(model);
            if (known != null) {
                return known;
            }
            JsonObject json = read(resources, model);
            if (json == null) {
                return ShelfTransform.NONE;
            }
            JsonObject display = GsonHelper.getAsJsonObject(json, "display", null);
            if (display != null && display.has("on_shelf")) {
                return ShelfTransform.parse(GsonHelper.getAsJsonObject(display, "on_shelf"));
            }
            model = json.has("parent") ? ResourceLocation.tryParse(GsonHelper.getAsString(json, "parent")) : null;
        }
        return ShelfTransform.NONE;
    }

    private static @Nullable JsonObject read(ResourceManager resources, ResourceLocation model) {
        Optional<Resource> resource = resources.getResource(model.withPath(p -> "models/" + p + ".json"));
        if (resource.isEmpty()) {
            return null;
        }
        try (Reader reader = resource.get().openAsReader()) {
            return GsonHelper.parse(reader);
        } catch (Exception broken) {
            return null;
        }
    }

    private record ShelfTransform(Vector3f rotation, Vector3f translation, Vector3f scale) {
        static final ShelfTransform NONE = new ShelfTransform(new Vector3f(), new Vector3f(), new Vector3f(1.0F, 1.0F, 1.0F));

        static ShelfTransform parse(JsonObject json) {
            Vector3f rotation = vector(json, "rotation", 0.0F);
            Vector3f translation = vector(json, "translation", 0.0F).mul(0.0625F);
            translation.set(Mth.clamp(translation.x, -5.0F, 5.0F), Mth.clamp(translation.y, -5.0F, 5.0F), Mth.clamp(translation.z, -5.0F, 5.0F));
            Vector3f scale = vector(json, "scale", 1.0F);
            scale.set(Mth.clamp(scale.x, -4.0F, 4.0F), Mth.clamp(scale.y, -4.0F, 4.0F), Mth.clamp(scale.z, -4.0F, 4.0F));
            return new ShelfTransform(rotation, translation, scale);
        }

        private static Vector3f vector(JsonObject json, String key, float fallback) {
            if (!json.has(key)) {
                return new Vector3f(fallback);
            }
            JsonArray array = GsonHelper.getAsJsonArray(json, key);
            return new Vector3f(GsonHelper.convertToFloat(array.get(0), key), GsonHelper.convertToFloat(array.get(1), key),
                GsonHelper.convertToFloat(array.get(2), key));
        }

        private Quaternionf quaternion() {
            return new Quaternionf().rotationXYZ(this.rotation.x * Mth.DEG_TO_RAD, this.rotation.y * Mth.DEG_TO_RAD, this.rotation.z * Mth.DEG_TO_RAD);
        }

        void apply(PoseStack poseStack) {
            if (this == NONE) {
                return;
            }
            poseStack.translate(this.translation.x, this.translation.y, this.translation.z);
            poseStack.mulPose(this.quaternion());
            poseStack.scale(this.scale.x, this.scale.y, this.scale.z);
        }

        Matrix4f matrix() {
            Matrix4f matrix = new Matrix4f();
            if (this != NONE) {
                matrix.translate(this.translation).rotate(this.quaternion()).scale(this.scale);
            }
            return matrix.translate(-0.5F, -0.5F, -0.5F);
        }
    }

    private static final class ExtentCollector implements MultiBufferSource, VertexConsumer {
        private double minX = Double.POSITIVE_INFINITY;
        private double minY = Double.POSITIVE_INFINITY;
        private double minZ = Double.POSITIVE_INFINITY;
        private double maxX = Double.NEGATIVE_INFINITY;
        private double maxY = Double.NEGATIVE_INFINITY;
        private double maxZ = Double.NEGATIVE_INFINITY;

        @Override
        public VertexConsumer getBuffer(RenderType type) {
            return this;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.minX = Math.min(this.minX, x);
            this.minY = Math.min(this.minY, y);
            this.minZ = Math.min(this.minZ, z);
            this.maxX = Math.max(this.maxX, x);
            this.maxY = Math.max(this.maxY, y);
            this.maxZ = Math.max(this.maxZ, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return this;
        }

        AABB box() {
            return this.minX <= this.maxX ? new AABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ) : new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
