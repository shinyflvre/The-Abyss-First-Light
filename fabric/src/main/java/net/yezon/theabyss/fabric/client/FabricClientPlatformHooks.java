package net.yezon.theabyss.fabric.client;

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
import net.yezon.theabyss.platform.services.ClientPlatformHooks;

public final class FabricClientPlatformHooks implements ClientPlatformHooks {
    @Override
    public float cameraRoll(Camera camera) {
        return FabricCameraRoll.get(camera);
    }

    @Override
    public float detachedCameraDistance(Camera camera, boolean flipped, float entityScale, float distance) {
        return distance;
    }

    @Override
    public boolean preventFlinch(LivingEntity entity) {
        return false;
    }

    @Override
    public CompletableFuture<AudioStream> soundStream(SoundInstance sound, SoundBufferLibrary library, Sound resolved, boolean looping) {
        return sound.getAudioStream(library, resolved.getPath(), looping);
    }

    @Override
    public CompletableFuture<AudioStream> audioStream(SoundInstance sound, SoundBufferLibrary library, ResourceLocation id, boolean looping) {
        return sound.getAudioStream(library, id, looping);
    }

    @Override
    public void matchStencil(RenderTarget source, RenderTarget target) {
    }

    @Override
    public BakedModel dynamicModel(DynamicBlockStateModel model) {
        return new FabricDynamicBlockStateModel(model);
    }
}
