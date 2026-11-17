package net.yezon.theabyss.fabric.client;

import com.google.common.collect.MapMaker;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.material.ShadeMode;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class FabricModelExtras {
    private static final Map<BlockElement, int[]> ELEMENT_LIGHT = new MapMaker().weakKeys().makeMap();
    private static final Map<BlockModel, BlendMode> RENDER_TYPES = new MapMaker().weakKeys().makeMap();
    private static final Set<BakedQuad> LIT_QUADS = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());
    private static final int STRIDE = DefaultVertexFormat.BLOCK.getVertexSize() / 4;
    private static final int UV2 = DefaultVertexFormat.BLOCK.getOffset(VertexFormatElement.UV2) / 4;

    private FabricModelExtras() {
    }

    static void register() {
        ModelLoadingPlugin.register(context -> context.modifyModelAfterBake().register(FabricModelExtras::afterBake));
    }

    public static void readElement(BlockElement element, JsonElement json) {
        if (!json.isJsonObject()) {
            return;
        }
        JsonObject object = json.getAsJsonObject();
        int base = light(object.get("neoforge_data"), 0);
        JsonObject faces = GsonHelper.getAsJsonObject(object, "faces", null);
        int[] lights = new int[6];
        boolean any = false;
        for (Direction direction : Direction.values()) {
            int light = base;
            if (faces != null && faces.get(direction.getName()) instanceof JsonObject face && face.has("neoforge_data")) {
                light = light(face.get("neoforge_data"), base);
            }
            lights[direction.get3DDataValue()] = light;
            any |= light != 0;
        }
        if (any) {
            ELEMENT_LIGHT.put(element, lights);
        }
    }

    public static void readModel(BlockModel model, JsonElement json) {
        if (json.isJsonObject() && json.getAsJsonObject().get("render_type") instanceof JsonElement type && type.isJsonPrimitive()) {
            RENDER_TYPES.put(model, blendMode(type.getAsString()));
        }
    }

    public static BakedQuad bakeFace(BakedQuad quad, BlockElement element, Direction facing) {
        int[] lights = ELEMENT_LIGHT.get(element);
        if (lights == null) {
            return quad;
        }
        int packed = lights[facing.get3DDataValue()];
        if (packed == 0) {
            return quad;
        }
        int[] vertices = quad.getVertices();
        for (int vertex = 0; vertex < 4; vertex++) {
            vertices[vertex * STRIDE + UV2] = packed;
        }
        LIT_QUADS.add(quad);
        return quad;
    }

    private static int light(@Nullable JsonElement data, int fallback) {
        if (data == null) {
            return fallback;
        }
        if (!data.isJsonObject()) {
            throw new JsonParseException("Expected neoforge_data to be an object");
        }
        JsonObject object = data.getAsJsonObject();
        int block = GsonHelper.getAsInt(object, "block_light", 0);
        int sky = GsonHelper.getAsInt(object, "sky_light", 0);
        if (block < 0 || block > 15 || sky < 0 || sky > 15) {
            throw new JsonParseException("neoforge_data light values must be between 0 and 15");
        }
        return LightTexture.pack(block, sky);
    }

    private static BlendMode blendMode(String name) {
        return switch (ResourceLocation.parse(name.toLowerCase(Locale.ROOT)).toString()) {
            case "minecraft:solid" -> BlendMode.SOLID;
            case "minecraft:cutout_mipped", "minecraft:cutout_mipped_all" -> BlendMode.CUTOUT_MIPPED;
            case "minecraft:cutout" -> BlendMode.CUTOUT;
            case "minecraft:translucent" -> BlendMode.TRANSLUCENT;
            default -> BlendMode.DEFAULT;
        };
    }

    private static BlendMode blendMode(UnbakedModel source) {
        BlockModel model = source instanceof BlockModel blockModel ? blockModel : null;
        for (int depth = 0; model != null && depth < 64; depth++) {
            BlendMode mode = RENDER_TYPES.get(model);
            if (mode != null) {
                return mode;
            }
            model = ((FabricBlockModelParent)model).theabyss$parent();
        }
        return BlendMode.DEFAULT;
    }

    private static boolean lit(BakedModel model) {
        RandomSource random = RandomSource.create(42L);
        for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
            for (BakedQuad quad : model.getQuads(null, ModelHelper.faceFromIndex(i), random)) {
                if (LIT_QUADS.contains(quad)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static BakedModel afterBake(BakedModel model, ModelModifier.AfterBake.Context context) {
        if (!(model instanceof SimpleBakedModel) || !RendererAccess.INSTANCE.hasRenderer() || !lit(model)) {
            return model;
        }
        return new LitModel(model, blendMode(context.sourceModel()), RendererAccess.INSTANCE.getRenderer());
    }

    private static final class LitModel extends ForwardingBakedModel {
        private final RenderMaterial standard;
        private final RenderMaterial noAo;

        LitModel(BakedModel model, BlendMode mode, Renderer renderer) {
            super(model);
            MaterialFinder finder = renderer.materialFinder();
            this.standard = finder.clear().shadeMode(ShadeMode.VANILLA).blendMode(mode).find();
            this.noAo = finder.clear().shadeMode(ShadeMode.VANILLA).blendMode(mode).ambientOcclusion(TriState.FALSE).find();
        }

        @Override
        public boolean isVanillaAdapter() {
            return false;
        }

        @Override
        public void emitBlockQuads(BlockAndTintGetter level, BlockState state, BlockPos pos, Supplier<RandomSource> random, RenderContext context) {
            QuadEmitter emitter = context.getEmitter();
            RenderMaterial material = this.wrapped.useAmbientOcclusion() ? this.standard : this.noAo;
            for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
                Direction face = ModelHelper.faceFromIndex(i);
                if (!context.hasTransform() && context.isFaceCulled(face)) {
                    continue;
                }
                for (BakedQuad quad : this.wrapped.getQuads(state, face, random.get())) {
                    emitter.fromVanilla(quad, material, face);
                    emitter.emit();
                }
            }
        }

        @Override
        public void emitItemQuads(ItemStack stack, Supplier<RandomSource> random, RenderContext context) {
            QuadEmitter emitter = context.getEmitter();
            for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
                Direction face = ModelHelper.faceFromIndex(i);
                for (BakedQuad quad : this.wrapped.getQuads(null, face, random.get())) {
                    emitter.fromVanilla(quad, this.standard, face);
                    emitter.emit();
                }
            }
        }
    }
}
