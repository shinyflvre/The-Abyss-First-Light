package net.yezon.theabyss.fabric.event;

import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.commands.RenderPass;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.platform.client.event.AddSectionGeometryEvent;
import net.yezon.theabyss.platform.client.event.ClientPlayerNetworkEvent;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.platform.client.event.ComputeFovModifierEvent;
import net.yezon.theabyss.platform.client.event.CustomizeGuiOverlayEvent;
import net.yezon.theabyss.platform.client.event.ExtractLevelRenderStateEvent;
import net.yezon.theabyss.platform.client.event.InputEvent;
import net.yezon.theabyss.platform.client.event.ModelEvent;
import net.yezon.theabyss.platform.client.event.MovementInputUpdateEvent;
import net.yezon.theabyss.platform.client.event.RenderArmEvent;
import net.yezon.theabyss.platform.client.event.RenderFrameEvent;
import net.yezon.theabyss.platform.client.event.RenderGuiEvent;
import net.yezon.theabyss.platform.client.event.RenderGuiLayerEvent;
import net.yezon.theabyss.platform.client.event.RenderHandEvent;
import net.yezon.theabyss.platform.client.event.RenderLevelStageEvent;
import net.yezon.theabyss.platform.client.event.RenderLivingEvent;
import net.yezon.theabyss.platform.client.event.RenderPlayerEvent;
import net.yezon.theabyss.platform.client.event.ScreenEvent;
import net.yezon.theabyss.platform.client.event.SelectMusicEvent;
import net.yezon.theabyss.platform.client.event.SubmitCustomGeometryEvent;
import net.yezon.theabyss.platform.client.event.TextureAtlasStitchedEvent;
import net.yezon.theabyss.platform.client.event.ViewportEvent;
import net.yezon.theabyss.platform.client.event.sound.PlaySoundEvent;
import net.yezon.theabyss.platform.client.event.sound.PlaySoundSourceEvent;
import net.yezon.theabyss.platform.client.event.sound.PlayStreamingSourceEvent;
import net.yezon.theabyss.platform.client.event.sound.SoundEngineLoadEvent;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.event.entity.player.ItemTooltipEvent;
import net.yezon.theabyss.platform.fml.event.lifecycle.FMLClientSetupEvent;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
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

    public static void movementInput(ClientInput input) {
        if (AbyssEventBus.hasListeners(MovementInputUpdateEvent.class)) {
            AbyssEventBus.post(new MovementInputUpdateEvent(input));
        }
    }

    public static float fovModifier(Player player, float modifier) {
        if (!AbyssEventBus.hasListeners(ComputeFovModifierEvent.class)) {
            return modifier;
        }
        return AbyssEventBus.post(new ComputeFovModifierEvent(modifier, player)).getNewFovModifier();
    }

    public static float fov(Camera camera, float partialTick, float fov) {
        if (!AbyssEventBus.hasListeners(ViewportEvent.ComputeFov.class)) {
            return fov;
        }
        return AbyssEventBus.post(new ViewportEvent.ComputeFov(camera, fov, partialTick)).getFOV();
    }

    public static ViewportEvent.@Nullable ComputeCameraAngles cameraAngles(float partialTick, float yaw, float pitch) {
        if (!AbyssEventBus.hasListeners(ViewportEvent.ComputeCameraAngles.class)) {
            return null;
        }
        return AbyssEventBus.post(new ViewportEvent.ComputeCameraAngles(partialTick, pitch, 0.0F, yaw));
    }

    public static void fogColor(Vector4f color) {
        if (AbyssEventBus.hasListeners(ViewportEvent.ComputeFogColor.class)) {
            ViewportEvent.ComputeFogColor event = AbyssEventBus.post(new ViewportEvent.ComputeFogColor(color.z, color.y, color.x));
            color.x = event.getRed();
            color.y = event.getGreen();
            color.z = event.getBlue();
        }
    }

    public static boolean listensToRenderFog() {
        return AbyssEventBus.hasListeners(ViewportEvent.RenderFog.class);
    }

    public static void renderFog(@Nullable FogEnvironment environment, FogData fog) {
        if (AbyssEventBus.hasListeners(ViewportEvent.RenderFog.class)) {
            ViewportEvent.RenderFog event = AbyssEventBus.post(new ViewportEvent.RenderFog(environment, fog.environmentalEnd, fog.environmentalStart));
            fog.environmentalEnd = event.getFarPlaneDistance();
            fog.environmentalStart = event.getNearPlaneDistance();
        }
    }

    public static void extractLevel(Camera camera, DeltaTracker deltaTracker, Frustum frustum, ClientLevel level, OptionsRenderState options,
        LevelRenderState renderState) {
        if (AbyssEventBus.hasListeners(ExtractLevelRenderStateEvent.class)) {
            AbyssEventBus.post(new ExtractLevelRenderStateEvent(camera, deltaTracker, frustum, level, options, renderState));
        }
    }

    public static void sectionGeometry(Level level, BlockPos origin) {
        if (AbyssEventBus.hasListeners(AddSectionGeometryEvent.class)) {
            AbyssEventBus.post(new AddSectionGeometryEvent(level, origin));
        }
    }

    public static void submitCustomGeometry(LevelRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector) {
        if (AbyssEventBus.hasListeners(SubmitCustomGeometryEvent.class)) {
            AbyssEventBus.post(new SubmitCustomGeometryEvent(renderState, poseStack, collector));
        }
    }

    public static void afterOpaqueBlocks(LevelRenderState renderState, Matrix4fc modelViewMatrix, RenderPass renderPass) {
        if (AbyssEventBus.hasListeners(RenderLevelStageEvent.AfterOpaqueBlocks.class)) {
            AbyssEventBus.post(new RenderLevelStageEvent.AfterOpaqueBlocks(renderState, modelViewMatrix, renderPass));
        }
    }

    public static void screenRenderPost(GuiGraphicsExtractor graphics, Screen screen) {
        if (AbyssEventBus.hasListeners(ScreenEvent.Render.Post.class)) {
            AbyssEventBus.post(new ScreenEvent.Render.Post(graphics, screen));
        }
    }

    public static boolean guiActive() {
        return !(Minecraft.getInstance().gui.screen() instanceof LevelLoadingScreen);
    }

    public static boolean renderGuiPre() {
        return AbyssEventBus.hasListeners(RenderGuiEvent.Pre.class) && AbyssEventBus.post(new RenderGuiEvent.Pre()).isCanceled();
    }

    public static void renderGuiPost() {
        if (AbyssEventBus.hasListeners(RenderGuiEvent.Post.class)) {
            AbyssEventBus.post(new RenderGuiEvent.Post());
        }
    }

    public static boolean guiLayerPre(GuiGraphicsExtractor graphics, Identifier layer) {
        return AbyssEventBus.hasListeners(RenderGuiLayerEvent.Pre.class) && AbyssEventBus.post(new RenderGuiLayerEvent.Pre(graphics, layer)).isCanceled();
    }

    public static void guiLayerPost(GuiGraphicsExtractor graphics, Identifier layer) {
        if (AbyssEventBus.hasListeners(RenderGuiLayerEvent.Post.class)) {
            AbyssEventBus.post(new RenderGuiLayerEvent.Post(graphics, layer));
        }
    }

    public static void guiLayerSkipped(GuiGraphicsExtractor graphics, Identifier... layers) {
        for (Identifier layer : layers) {
            if (!guiLayerPre(graphics, layer)) {
                guiLayerPost(graphics, layer);
            }
        }
    }

    public static boolean bossEventProgress(LerpingBossEvent bossEvent, GuiGraphicsExtractor graphics, int x, int y) {
        return AbyssEventBus.hasListeners(CustomizeGuiOverlayEvent.BossEventProgress.class)
            && AbyssEventBus.post(new CustomizeGuiOverlayEvent.BossEventProgress(bossEvent, graphics, x, y)).isCanceled();
    }

    public static boolean renderHand(float equipProgress, InteractionHand hand, ItemStack stack, PlayerRenderState playerState, PoseStack poseStack,
        SubmitNodeCollector collector, float swingProgress) {
        if (!AbyssEventBus.hasListeners(RenderHandEvent.class)) {
            return false;
        }
        int light = playerState.avatarRenderState.lightCoords;
        return AbyssEventBus.post(new RenderHandEvent(equipProgress, hand, stack, light, playerState, poseStack, collector, swingProgress)).isCanceled();
    }

    public static boolean renderArm(HumanoidArm arm, int lightCoords, PlayerRenderState playerState, PoseStack poseStack, Identifier skinTexture,
        SubmitNodeCollector collector, boolean hasSleeve) {
        return AbyssEventBus.hasListeners(RenderArmEvent.class)
            && AbyssEventBus.post(new RenderArmEvent(arm, lightCoords, playerState, poseStack, skinTexture, collector, hasSleeve)).isCanceled();
    }

    public static boolean renderPlayerPre(PoseStack poseStack, LivingEntityRenderState state) {
        return AbyssEventBus.hasListeners(RenderPlayerEvent.Pre.class) && AbyssEventBus.post(new RenderPlayerEvent.Pre(poseStack, state)).isCanceled();
    }

    public static void renderPlayerPost(PoseStack poseStack, LivingEntityRenderState state) {
        if (AbyssEventBus.hasListeners(RenderPlayerEvent.Post.class)) {
            AbyssEventBus.post(new RenderPlayerEvent.Post(poseStack, state));
        }
    }

    public static boolean renderLivingPre(PoseStack poseStack, LivingEntityRenderState state) {
        return AbyssEventBus.hasListeners(RenderLivingEvent.Pre.class) && AbyssEventBus.post(new RenderLivingEvent.Pre(poseStack, state)).isCanceled();
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

    public static void modifyBakingResult(ModelBakery.BakingResult result) {
        if (AbyssEventBus.hasListeners(ModelEvent.ModifyBakingResult.class)) {
            AbyssEventBus.post(new ModelEvent.ModifyBakingResult(result));
        }
    }
}
