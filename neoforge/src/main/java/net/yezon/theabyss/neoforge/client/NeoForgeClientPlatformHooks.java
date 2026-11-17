package net.yezon.theabyss.neoforge.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Camera;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.common.Tags;
import net.yezon.theabyss.platform.client.model.DynamicBlockStateModel;
import net.yezon.theabyss.platform.services.ClientPlatformHooks;

public final class NeoForgeClientPlatformHooks implements ClientPlatformHooks {
    @Override
    public float cameraRoll(Camera camera) {
        return camera.getRoll();
    }

    @Override
    public float detachedCameraDistance(Camera camera, boolean flipped, float entityScale, float distance) {
        return ClientHooks.getDetachedCameraDistance(camera, flipped, entityScale, distance);
    }

    @Override
    public boolean preventFlinch(LivingEntity entity) {
        DamageSource source = entity.getLastDamageSource();
        return source != null && source.is(Tags.DamageTypes.NO_FLINCH);
    }

    @Override
    public CompletableFuture<AudioStream> soundStream(SoundInstance sound, SoundBufferLibrary library, Sound resolved, boolean looping) {
        return sound.getStream(library, resolved, looping);
    }

    @Override
    public CompletableFuture<AudioStream> audioStream(SoundInstance sound, SoundBufferLibrary library, ResourceLocation id, boolean looping) {
        return library.getStream(id, looping);
    }

    @Override
    public void matchStencil(RenderTarget source, RenderTarget target) {
        if (source.isStencilEnabled() && !target.isStencilEnabled()) {
            target.enableStencil();
        }
    }

    @Override
    public BakedModel dynamicModel(DynamicBlockStateModel model) {
        return new NeoForgeDynamicBlockStateModel(model);
    }
}
