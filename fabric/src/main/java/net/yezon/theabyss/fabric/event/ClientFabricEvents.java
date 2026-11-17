package net.yezon.theabyss.fabric.event;

import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.Music;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.yezon.theabyss.platform.client.event.AddSectionGeometryEvent;
import net.yezon.theabyss.platform.client.event.ClientPlayerNetworkEvent;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.platform.client.event.ComputeFovModifierEvent;
import net.yezon.theabyss.platform.client.event.CustomizeGuiOverlayEvent;
import net.yezon.theabyss.platform.client.event.InputEvent;
import net.yezon.theabyss.platform.client.event.ModelEvent;
import net.yezon.theabyss.platform.client.event.MovementInputUpdateEvent;
import net.yezon.theabyss.platform.client.event.RenderArmEvent;
import net.yezon.theabyss.platform.client.event.RenderFrameEvent;
import net.yezon.theabyss.platform.client.event.RenderHandEvent;
import net.yezon.theabyss.platform.client.event.RenderLevelStageEvent;
import net.yezon.theabyss.platform.client.event.RenderLivingEvent;
import net.yezon.theabyss.platform.client.event.RenderPlayerEvent;
import net.yezon.theabyss.platform.client.event.SelectMusicEvent;
import net.yezon.theabyss.platform.client.event.TextureAtlasStitchedEvent;
import net.yezon.theabyss.platform.client.event.ViewportEvent;
import net.yezon.theabyss.platform.client.event.sound.PlaySoundEvent;
import net.yezon.theabyss.platform.client.event.sound.PlaySoundSourceEvent;
import net.yezon.theabyss.platform.client.event.sound.PlayStreamingSourceEvent;
import net.yezon.theabyss.platform.client.event.sound.SoundEngineLoadEvent;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.event.entity.player.ItemTooltipEvent;
import net.yezon.theabyss.platform.fml.event.lifecycle.FMLClientSetupEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public final class ClientFabricEvents {
    private ClientFabricEvents() {
    }

    public static void register() {
        if (AbyssEventBus.hasListeners(FMLClientSetupEvent.class)) {
            AbyssEventBus.post(new FMLClientSetupEvent());
        }
    }

    public static void clientTickPre() {
        if (AbyssEventBus.hasListeners(ClientTickEvent.Pre.class)) {
            AbyssEventBus.post(new ClientTickEvent.Pre());
        }
    }

    public static void clientTickPost() {
        if (AbyssEventBus.hasListeners(ClientTickEvent.Post.class)) {
            AbyssEventBus.post(new ClientTickEvent.Post());
        }
    }

    public static void renderFramePre(DeltaTracker deltaTracker) {
        if (AbyssEventBus.hasListeners(RenderFrameEvent.Pre.class)) {
            AbyssEventBus.post(new RenderFrameEvent.Pre(deltaTracker));
        }
    }

    public static void loggingIn() {
        if (AbyssEventBus.hasListeners(ClientPlayerNetworkEvent.LoggingIn.class)) {
            AbyssEventBus.post(new ClientPlayerNetworkEvent.LoggingIn());
        }
    }

    public static void loggingOut() {
        if (AbyssEventBus.hasListeners(ClientPlayerNetworkEvent.LoggingOut.class)) {
            AbyssEventBus.post(new ClientPlayerNetworkEvent.LoggingOut());
        }
    }

    public static InputEvent.@Nullable InteractionKeyMappingTriggered interaction(int button, InteractionHand hand) {
        if (!AbyssEventBus.hasListeners(InputEvent.InteractionKeyMappingTriggered.class)) {
            return null;
        }
        return AbyssEventBus.post(new InputEvent.InteractionKeyMappingTriggered(hand, button == 0, button == 2, button == 1, true));
    }

    public static boolean mouseScrolling(double scrollDeltaY) {
        return AbyssEventBus.hasListeners(InputEvent.MouseScrollingEvent.class)
            && AbyssEventBus.post(new InputEvent.MouseScrollingEvent(scrollDeltaY)).isCanceled();
    }

    public static void movementInput(Player player, Input input) {
        if (AbyssEventBus.hasListeners(MovementInputUpdateEvent.class)) {
            AbyssEventBus.post(new MovementInputUpdateEvent(player, input));
        }
    }

    public static float fovModifier(Player player, float modifier) {
        if (!AbyssEventBus.hasListeners(ComputeFovModifierEvent.class)) {
            return modifier;
        }
        return AbyssEventBus.post(new ComputeFovModifierEvent(modifier, player)).getNewFovModifier();
    }

    public static double fov(Camera camera, float partialTick, double fov, boolean usedConfiguredFov) {
        if (!AbyssEventBus.hasListeners(ViewportEvent.ComputeFov.class)) {
            return fov;
        }
        float value = (float)fov;
        float result = AbyssEventBus.post(new ViewportEvent.ComputeFov(camera, partialTick, value, usedConfiguredFov)).getFOV();
        return result != value ? result : fov;
    }

    public static ViewportEvent.@Nullable ComputeCameraAngles cameraAngles(Camera camera, float partialTick, float yaw, float pitch) {
        if (!AbyssEventBus.hasListeners(ViewportEvent.ComputeCameraAngles.class)) {
            return null;
        }
        return AbyssEventBus.post(new ViewportEvent.ComputeCameraAngles(camera, partialTick, yaw, pitch, 0.0F));
    }

    public static boolean listensToFogColor() {
        return AbyssEventBus.hasListeners(ViewportEvent.ComputeFogColor.class);
    }

    public static Vector3f fogColor(Camera camera, float partialTick, float red, float green, float blue) {
        ViewportEvent.ComputeFogColor event = AbyssEventBus.post(new ViewportEvent.ComputeFogColor(camera, partialTick, red, green, blue));
        return new Vector3f(event.getRed(), event.getGreen(), event.getBlue());
    }

    public static void renderFog(FogRenderer.FogMode mode, Camera camera, float partialTick) {
        if (!AbyssEventBus.hasListeners(ViewportEvent.RenderFog.class)) {
            return;
        }
        FogType type = camera.getFluidInCamera();
        ViewportEvent.RenderFog event = AbyssEventBus.post(new ViewportEvent.RenderFog(mode, type, camera, partialTick, RenderSystem.getShaderFogStart(),
            RenderSystem.getShaderFogEnd(), RenderSystem.getShaderFogShape()));
        if (event.isChanged()) {
            RenderSystem.setShaderFogStart(event.getNearPlaneDistance());
            RenderSystem.setShaderFogEnd(event.getFarPlaneDistance());
            FogShape shape = event.getFogShape();
            RenderSystem.setShaderFogShape(shape);
        }
    }

    public static boolean listensToLevelStages() {
        return AbyssEventBus.hasListeners(RenderLevelStageEvent.class);
    }

    public static void levelStage(RenderLevelStageEvent.Stage stage, LevelRenderer levelRenderer, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix,
        Matrix4f projectionMatrix, int renderTick, Camera camera, Frustum frustum) {
        if (AbyssEventBus.hasListeners(RenderLevelStageEvent.class)) {
            Minecraft mc = Minecraft.getInstance();
            mc.getProfiler().push(stage.toString());
            AbyssEventBus.post(new RenderLevelStageEvent(stage, levelRenderer, poseStack, modelViewMatrix, projectionMatrix, renderTick, mc.getTimer(), camera,
                frustum));
            mc.getProfiler().pop();
        }
    }

    public static void sectionGeometry(Level level, BlockPos origin) {
        if (AbyssEventBus.hasListeners(AddSectionGeometryEvent.class)) {
            AbyssEventBus.post(new AddSectionGeometryEvent(level, origin));
        }
    }

    public static CustomizeGuiOverlayEvent.@Nullable BossEventProgress bossEventProgress(GuiGraphics graphics, LerpingBossEvent bossEvent, int x, int y,
        int increment) {
        if (!AbyssEventBus.hasListeners(CustomizeGuiOverlayEvent.BossEventProgress.class)) {
            return null;
        }
        return AbyssEventBus.post(new CustomizeGuiOverlayEvent.BossEventProgress(graphics, Minecraft.getInstance().getTimer(), bossEvent, x, y, increment));
    }

    public static boolean renderHand(InteractionHand hand, PoseStack poseStack, MultiBufferSource buffers, int packedLight, float partialTick,
        float interpolatedPitch, float swingProgress, float equipProgress, ItemStack stack) {
        return AbyssEventBus.hasListeners(RenderHandEvent.class) && AbyssEventBus.post(new RenderHandEvent(hand, poseStack, buffers, packedLight, partialTick,
            interpolatedPitch, swingProgress, equipProgress, stack)).isCanceled();
    }

    public static boolean renderArm(PoseStack poseStack, MultiBufferSource buffers, int packedLight, AbstractClientPlayer player, HumanoidArm arm) {
        return AbyssEventBus.hasListeners(RenderArmEvent.class)
            && AbyssEventBus.post(new RenderArmEvent(poseStack, buffers, packedLight, player, arm)).isCanceled();
    }

    public static boolean renderPlayerPre(Player player, PlayerRenderer renderer, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
        int packedLight) {
        return AbyssEventBus.hasListeners(RenderPlayerEvent.Pre.class)
            && AbyssEventBus.post(new RenderPlayerEvent.Pre(player, renderer, partialTick, poseStack, buffers, packedLight)).isCanceled();
    }

    public static void renderPlayerPost(Player player, PlayerRenderer renderer, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
        int packedLight) {
        if (AbyssEventBus.hasListeners(RenderPlayerEvent.Post.class)) {
            AbyssEventBus.post(new RenderPlayerEvent.Post(player, renderer, partialTick, poseStack, buffers, packedLight));
        }
    }

    public static boolean renderLivingPre(LivingEntity entity, LivingEntityRenderer<?, ?> renderer, float partialTick, PoseStack poseStack,
        MultiBufferSource buffers, int packedLight) {
        return AbyssEventBus.hasListeners(RenderLivingEvent.Pre.class)
            && AbyssEventBus.post(new RenderLivingEvent.Pre(entity, renderer, partialTick, poseStack, buffers, packedLight)).isCanceled();
    }

    public static void renderLivingPost(LivingEntity entity, LivingEntityRenderer<?, ?> renderer, float partialTick, PoseStack poseStack,
        MultiBufferSource buffers, int packedLight) {
        if (AbyssEventBus.hasListeners(RenderLivingEvent.Post.class)) {
            AbyssEventBus.post(new RenderLivingEvent.Post(entity, renderer, partialTick, poseStack, buffers, packedLight));
        }
    }

    public static @Nullable SoundInstance playSound(SoundInstance sound) {
        if (!AbyssEventBus.hasListeners(PlaySoundEvent.class)) {
            return sound;
        }
        return AbyssEventBus.post(new PlaySoundEvent(sound, sound)).getSound();
    }

    public static boolean listensToSoundSources() {
        return AbyssEventBus.hasListeners(PlaySoundSourceEvent.class);
    }

    public static boolean listensToStreamingSources() {
        return AbyssEventBus.hasListeners(PlayStreamingSourceEvent.class);
    }

    public static void playSoundSource(Channel channel, SoundInstance sound) {
        if (AbyssEventBus.hasListeners(PlaySoundSourceEvent.class)) {
            AbyssEventBus.post(new PlaySoundSourceEvent(channel, sound));
        }
    }

    public static void playStreamingSource(Channel channel, SoundInstance sound) {
        if (AbyssEventBus.hasListeners(PlayStreamingSourceEvent.class)) {
            AbyssEventBus.post(new PlayStreamingSourceEvent(channel, sound));
        }
    }

    public static void soundEngineLoad() {
        if (AbyssEventBus.hasListeners(SoundEngineLoadEvent.class)) {
            AbyssEventBus.post(new SoundEngineLoadEvent());
        }
    }

    public static boolean listensToMusic() {
        return AbyssEventBus.hasListeners(SelectMusicEvent.class);
    }

    public static @Nullable Music selectMusic(Music situational) {
        return AbyssEventBus.post(new SelectMusicEvent(situational)).getMusic();
    }

    public static void itemTooltip(@Nullable Player player, TooltipFlag flag, ItemStack stack, List<Component> lines) {
        if (AbyssEventBus.hasListeners(ItemTooltipEvent.class)) {
            AbyssEventBus.post(new ItemTooltipEvent(player, flag, stack, lines));
        }
    }

    public static void atlasStitched(TextureAtlas atlas) {
        if (AbyssEventBus.hasListeners(TextureAtlasStitchedEvent.class)) {
            AbyssEventBus.post(new TextureAtlasStitchedEvent(atlas));
        }
    }

    public static void modifyBakingResult(Map<ModelResourceLocation, BakedModel> models, ModelBakery bakery) {
        if (AbyssEventBus.hasListeners(ModelEvent.ModifyBakingResult.class)) {
            AbyssEventBus.post(new ModelEvent.ModifyBakingResult(models, bakery));
        }
    }
}
