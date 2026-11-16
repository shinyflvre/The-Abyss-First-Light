package net.yezon.theabyss.platform.services;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
import org.jspecify.annotations.Nullable;

public interface ClientPlatformHooks {
    static ClientPlatformHooks get() {
        ClientPlatformHooks hooks = Instance.hooks;
        if (hooks == null) {
            throw new IllegalStateException("The Abyss client hooks were not installed by the loader");
        }
        return hooks;
    }

    static void install(ClientPlatformHooks hooks) {
        Instance.hooks = hooks;
    }

    <T> @Nullable T getRenderData(Object renderState, ContextKey<T> key);

    <T> void setRenderData(Object renderState, ContextKey<T> key, @Nullable T value);

    KeyMapping keyMapping(String name, KeyConflictContext context, KeyModifier modifier, InputConstants.Type type, int keyCode, KeyMapping.Category category);

    float cameraRoll(Camera camera);

    float detachedCameraDistance(Camera camera, boolean flipped, float entityScale, float entityDistance, float vehicleEntityScale, float vehicleDistance);

    RenderPipeline.Builder toBuilder(RenderPipeline pipeline);

    BlockModelResolver blockModelResolver(Minecraft minecraft);

    boolean preventFlinch(CameraEntityRenderState state);

    CompletableFuture<AudioStream> soundStream(SoundInstance sound, SoundBufferLibrary library, Sound resolved, boolean looping);

    CompletableFuture<AudioStream> audioStream(SoundInstance sound, SoundBufferLibrary library, Identifier id, boolean looping);

    int hudLeftHeight();

    void reserveHudRight(int minHeight);

    BlockStateModel dynamicModel(DynamicBlockStateModel model);

    void collectParts(BlockStateModel model, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts);

    Material.Baked particleMaterial(BlockStateModel model, BlockAndTintGetter level, BlockPos pos, BlockState state);

    final class Instance {
        private static volatile @Nullable ClientPlatformHooks hooks;

        private Instance() {
        }
    }
}
