package net.yezon.theabyss.fabric.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.state.level.CameraEntityRenderState;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.platform.client.model.DynamicBlockStateModel;
import net.yezon.theabyss.platform.client.settings.KeyConflictContext;
import net.yezon.theabyss.platform.client.settings.KeyModifier;
import net.yezon.theabyss.platform.services.ClientPlatformHooks;
import org.jspecify.annotations.Nullable;

public final class FabricClientPlatformHooks implements ClientPlatformHooks {
    private static final Map<ContextKey<?>, RenderStateDataKey<?>> KEYS = new ConcurrentHashMap<>();
    private volatile @Nullable BlockModelResolver blockModelResolver;

    @SuppressWarnings("unchecked")
    private static <T> RenderStateDataKey<T> key(ContextKey<T> key) {
        return (RenderStateDataKey<T>)KEYS.computeIfAbsent(key, k -> RenderStateDataKey.create(k::toString));
    }

    @Override
    public <T> @Nullable T getRenderData(Object renderState, ContextKey<T> key) {
        return ((FabricRenderState)renderState).getData(key(key));
    }

    @Override
    public <T> void setRenderData(Object renderState, ContextKey<T> key, @Nullable T value) {
        ((FabricRenderState)renderState).setData(key(key), value);
    }

    @Override
    public KeyMapping keyMapping(String name, KeyConflictContext context, KeyModifier modifier, InputConstants.Type type, int keyCode,
        KeyMapping.Category category) {
        return new KeyMapping(name, type, keyCode, category);
    }

    @Override
    public float cameraRoll(Camera camera) {
        return FabricCameraRoll.get(camera);
    }

    @Override
    public float detachedCameraDistance(Camera camera, boolean flipped, float entityScale, float entityDistance, float vehicleEntityScale,
        float vehicleDistance) {
        return Math.max(entityScale * entityDistance, vehicleEntityScale * vehicleDistance);
    }

    @Override
    public RenderPipeline.Builder toBuilder(RenderPipeline pipeline) {
        List<ColorTargetState> targets = pipeline.getColorTargetStates();
        List<VertexFormat> formats = pipeline.getVertexFormatBindings();
        ColorTargetState[] colorTargetStates = new ColorTargetState[targets.size()];
        int active = 0;
        for (int i = 0; i < targets.size(); i++) {
            colorTargetStates[i] = targets.get(i);
            if (targets.get(i) != null) {
                active = i + 1;
            }
        }
        VertexFormat[] vertexFormats = new VertexFormat[formats.size()];
        for (int i = 0; i < formats.size(); i++) {
            vertexFormats[i] = formats.get(i);
        }
        RenderPipeline.Snippet snippet = new RenderPipeline.Snippet(
            pipeline.getShaders(),
            pipeline.getShaderDefines().isEmpty() ? Optional.empty() : Optional.of(pipeline.getShaderDefines()),
            pipeline.getBindGroupLayouts().isEmpty() ? Optional.empty() : Optional.of(pipeline.getBindGroupLayouts()),
            colorTargetStates,
            active,
            Optional.ofNullable(pipeline.getDepthStencilState()),
            Optional.of(pipeline.getPolygonMode()),
            Optional.of(pipeline.isCull()),
            vertexFormats,
            Optional.of(pipeline.getPrimitiveTopology()),
            pipeline.pushConstantSize()
        );
        return RenderPipeline.builder(snippet).withLocation(pipeline.getLocation());
    }

    @Override
    public BlockModelResolver blockModelResolver(Minecraft minecraft) {
        BlockModelResolver resolver = this.blockModelResolver;
        if (resolver == null) {
            resolver = new BlockModelResolver(minecraft.getModelManager());
            this.blockModelResolver = resolver;
        }
        return resolver;
    }

    @Override
    public boolean preventFlinch(CameraEntityRenderState state) {
        return false;
    }

    @Override
    public CompletableFuture<AudioStream> soundStream(SoundInstance sound, SoundBufferLibrary library, Sound resolved, boolean looping) {
        return sound.getAudioStream(library, resolved.getPath(), looping);
    }

    @Override
    public CompletableFuture<AudioStream> audioStream(SoundInstance sound, SoundBufferLibrary library, Identifier id, boolean looping) {
        return sound.getAudioStream(library, id, looping);
    }

    @Override
    public int hudLeftHeight() {
        return FabricHudHeights.left();
    }

    @Override
    public void reserveHudRight(int minHeight) {
        FabricHudHeights.reserveRight(minHeight);
    }

    @Override
    public BlockStateModel dynamicModel(DynamicBlockStateModel model) {
        return new FabricDynamicBlockStateModel(model);
    }

    @Override
    public void collectParts(BlockStateModel model, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
        List<BlockStateModelPart> parts) {
        model.collectParts(random, parts);
    }

    @Override
    public Material.Baked particleMaterial(BlockStateModel model, BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return model.particleMaterial(level, pos, state);
    }
}
