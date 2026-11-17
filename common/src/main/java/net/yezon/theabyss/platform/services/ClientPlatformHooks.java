package net.yezon.theabyss.platform.services;

import com.mojang.blaze3d.pipeline.RenderTarget;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Camera;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.platform.client.model.DynamicBlockStateModel;
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

    float cameraRoll(Camera camera);

    float detachedCameraDistance(Camera camera, boolean flipped, float entityScale, float distance);

    boolean preventFlinch(LivingEntity entity);

    CompletableFuture<AudioStream> soundStream(SoundInstance sound, SoundBufferLibrary library, Sound resolved, boolean looping);

    CompletableFuture<AudioStream> audioStream(SoundInstance sound, SoundBufferLibrary library, ResourceLocation id, boolean looping);

    void matchStencil(RenderTarget source, RenderTarget target);

    BakedModel dynamicModel(DynamicBlockStateModel model);

    final class Instance {
        private static volatile @Nullable ClientPlatformHooks hooks;

        private Instance() {
        }
    }
}
