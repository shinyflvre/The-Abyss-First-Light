package net.yezon.theabyss.neoforge.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
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
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.extensions.IRenderStateExtension;
import net.yezon.theabyss.platform.client.model.DynamicBlockStateModel;
import net.yezon.theabyss.platform.client.settings.KeyConflictContext;
import net.yezon.theabyss.platform.client.settings.KeyModifier;
import net.yezon.theabyss.platform.services.ClientPlatformHooks;
import org.jspecify.annotations.Nullable;

public final class NeoForgeClientPlatformHooks implements ClientPlatformHooks {
    @Override
    public <T> @Nullable T getRenderData(Object renderState, ContextKey<T> key) {
        return ((IRenderStateExtension)renderState).getRenderData(key);
    }

    @Override
    public <T> void setRenderData(Object renderState, ContextKey<T> key, @Nullable T value) {
        ((IRenderStateExtension)renderState).setRenderData(key, value);
    }

    @Override
    public KeyMapping keyMapping(String name, KeyConflictContext context, KeyModifier modifier, InputConstants.Type type, int keyCode,
        KeyMapping.Category category) {
        return new KeyMapping(name, conflictContext(context), keyModifier(modifier), type, keyCode, category);
    }

    private static net.neoforged.neoforge.client.settings.KeyConflictContext conflictContext(KeyConflictContext context) {
        return switch (context) {
            case UNIVERSAL -> net.neoforged.neoforge.client.settings.KeyConflictContext.UNIVERSAL;
            case GUI -> net.neoforged.neoforge.client.settings.KeyConflictContext.GUI;
            case IN_GAME -> net.neoforged.neoforge.client.settings.KeyConflictContext.IN_GAME;
        };
    }

    private static net.neoforged.neoforge.client.settings.KeyModifier keyModifier(KeyModifier modifier) {
        return switch (modifier) {
            case CONTROL -> net.neoforged.neoforge.client.settings.KeyModifier.CONTROL;
            case SHIFT -> net.neoforged.neoforge.client.settings.KeyModifier.SHIFT;
            case ALT -> net.neoforged.neoforge.client.settings.KeyModifier.ALT;
            case NONE -> net.neoforged.neoforge.client.settings.KeyModifier.NONE;
        };
    }

    @Override
    public float cameraRoll(Camera camera) {
        return camera.getRoll();
    }

    @Override
    public float detachedCameraDistance(Camera camera, boolean flipped, float entityScale, float entityDistance, float vehicleEntityScale,
        float vehicleDistance) {
        return ClientHooks.getDetachedCameraDistance(camera, flipped, entityScale, entityDistance, vehicleEntityScale, vehicleDistance);
    }

    @Override
    public RenderPipeline.Builder toBuilder(RenderPipeline pipeline) {
        return pipeline.toBuilder();
    }

    @Override
    public BlockModelResolver blockModelResolver(Minecraft minecraft) {
        return minecraft.getBlockModelResolver();
    }

    @Override
    public boolean preventFlinch(CameraEntityRenderState state) {
        return state.preventFlinch;
    }

    @Override
    public CompletableFuture<AudioStream> soundStream(SoundInstance sound, SoundBufferLibrary library, Sound resolved, boolean looping) {
        return sound.getStream(library, resolved, looping);
    }

    @Override
    public CompletableFuture<AudioStream> audioStream(SoundInstance sound, SoundBufferLibrary library, Identifier id, boolean looping) {
        return library.getStream(id, looping);
    }

    @Override
    public int hudLeftHeight() {
        return Minecraft.getInstance().gui.hud.leftHeight;
    }

    @Override
    public void reserveHudRight(int minHeight) {
        Hud hud = Minecraft.getInstance().gui.hud;
        hud.rightHeight = Math.max(hud.rightHeight, minHeight);
    }

    @Override
    public BlockStateModel dynamicModel(DynamicBlockStateModel model) {
        return model;
    }

    @Override
    public void collectParts(BlockStateModel model, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
        List<BlockStateModelPart> parts) {
        model.collectParts(level, pos, state, random, parts);
    }

    @Override
    public Material.Baked particleMaterial(BlockStateModel model, BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return model.particleMaterial(level, pos, state);
    }
}
