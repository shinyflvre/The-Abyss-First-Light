package net.yezon.theabyss.client.fx;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.mixin.client.PostChainAccessor;
import net.yezon.theabyss.platform.client.event.RegisterShadersEvent;
import net.yezon.theabyss.platform.services.ClientPlatformHooks;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public final class PostFx {
    private static final List<ResourceLocation> ACTIVE = new ArrayList<>();
    private static final Map<ResourceLocation, PostChain> CHAINS = new HashMap<>();
    private static final Set<ResourceLocation> FAILED = new HashSet<>();
    private static final Set<ResourceLocation> APPLIED = new HashSet<>();
    private static final Map<PostChain, Long> SIZES = new HashMap<>();
    private static final Map<ResourceLocation, List<String>> PERSISTENT = Map.of(
        ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "dreamshift"), List.of("previous"));
    private static final Matrix4f PROJECTION = new Matrix4f();
    private static final ResourceLocation HAND_TEXTURE = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "textures/effect/hand_depth.png");
    private static @Nullable TextureTarget savedDepth;
    private static boolean depthSaved;
    private static @Nullable TextureTarget handDepth;
    private static boolean handPending;
    private static boolean handCaptured;
    private static @Nullable ShaderInstance depthMerge;
    private static @Nullable HandDepthTexture handTexture;

    private PostFx() {
    }

    public static List<ResourceLocation> activeEffects() {
        return ACTIVE;
    }

    public static Matrix4f levelProjection() {
        return PROJECTION;
    }

    public static void captureProjection(Matrix4f projection) {
        PROJECTION.set(projection);
    }

    public static @Nullable PostChain chain(ResourceLocation effect) {
        PostChain chain = CHAINS.get(effect);
        if (chain != null || FAILED.contains(effect)) {
            return chain;
        }
        Minecraft mc = Minecraft.getInstance();
        ScreenFx.prepareTextures(mc);
        ColoredLight.prepareTextures(mc);
        MagicLights.prepareTexture(mc);
        prepareHandTexture(mc);
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(effect.getNamespace(), "shaders/post/" + effect.getPath() + ".json");
        try {
            RenderTarget main = mc.getMainRenderTarget();
            chain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), main, location);
            chain.resize(main.width, main.height);
            SIZES.put(chain, size(main));
            CHAINS.put(effect, chain);
        } catch (IOException | RuntimeException e) {
            TheabyssMod.LOGGER.warn("Failed to load post effect {}", effect, e);
            FAILED.add(effect);
            chain = null;
        }
        return chain;
    }

    public static void reload() {
        for (PostChain chain : CHAINS.values()) {
            chain.close();
        }
        CHAINS.clear();
        SIZES.clear();
        FAILED.clear();
        APPLIED.clear();
    }

    public static void saveDepth() {
        depthSaved = false;
        if (ACTIVE.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        TextureTarget saved = savedDepth;
        if (saved == null) {
            saved = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
            ClientPlatformHooks.get().matchStencil(main, saved);
            savedDepth = saved;
        } else if (saved.width != main.width || saved.height != main.height) {
            saved.resize(main.width, main.height, Minecraft.ON_OSX);
        }
        saved.copyDepthFrom(main);
        main.bindWrite(false);
        depthSaved = true;
        handPending = true;
    }

    public static void captureHand() {
        if (ACTIVE.isEmpty()) {
            handPending = false;
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        TextureTarget hand = handTarget(main);
        if (handPending) {
            hand.copyDepthFrom(main);
        } else {
            hand.clear(Minecraft.ON_OSX);
        }
        main.bindWrite(false);
        handCaptured = handPending;
        handPending = false;
    }

    public static void registerShaders(RegisterShadersEvent event) {
        event.registerShader(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "depth_merge"), DefaultVertexFormat.POSITION,
            shader -> depthMerge = shader);
    }

    private static void mergeHand(RenderTarget main) {
        ShaderInstance shader = depthMerge;
        TextureTarget hand = handDepth;
        if (!handCaptured || shader == null || hand == null || hand.width != main.width || hand.height != main.height) {
            return;
        }
        main.bindWrite(false);
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(513);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, hand.getDepthTextureId());
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        builder.addVertex(-1.0F, -1.0F, 0.0F);
        builder.addVertex(1.0F, -1.0F, 0.0F);
        builder.addVertex(1.0F, 1.0F, 0.0F);
        builder.addVertex(-1.0F, 1.0F, 0.0F);
        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.depthFunc(515);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.disableDepthTest();
    }

    private static TextureTarget handTarget(RenderTarget main) {
        TextureTarget hand = handDepth;
        if (hand == null) {
            hand = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
            ClientPlatformHooks.get().matchStencil(main, hand);
            hand.clear(Minecraft.ON_OSX);
            handDepth = hand;
        } else if (hand.width != main.width || hand.height != main.height) {
            hand.resize(main.width, main.height, Minecraft.ON_OSX);
            hand.clear(Minecraft.ON_OSX);
        }
        return hand;
    }

    private static void prepareHandTexture(Minecraft mc) {
        if (handTexture != null) {
            return;
        }
        handTarget(mc.getMainRenderTarget());
        mc.getMainRenderTarget().bindWrite(false);
        HandDepthTexture texture = new HandDepthTexture();
        mc.getTextureManager().register(HAND_TEXTURE, texture);
        handTexture = texture;
    }

    private static final class HandDepthTexture extends net.minecraft.client.renderer.texture.AbstractTexture {
        @Override
        public void load(net.minecraft.server.packs.resources.ResourceManager manager) {
        }

        @Override
        public int getId() {
            TextureTarget hand = handDepth;
            return hand != null ? hand.getDepthTextureId() : super.getId();
        }
    }

    public static void apply(DeltaTracker deltaTracker) {
        Set<ResourceLocation> previous = new HashSet<>(APPLIED);
        APPLIED.clear();
        if (!ACTIVE.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            RenderTarget main = mc.getMainRenderTarget();
            restoreDepth(main);
            List<ResourceLocation> order = List.copyOf(ACTIVE);
            for (ResourceLocation effect : order) {
                PostChain chain = chain(effect);
                if (chain == null) {
                    continue;
                }
                try {
                    prepare(mc, chain);
                    RenderSystem.disableBlend();
                    RenderSystem.disableDepthTest();
                    RenderSystem.resetTextureMatrix();
                    chain.process(deltaTracker.getGameTimeDeltaTicks());
                    APPLIED.add(effect);
                    previous.remove(effect);
                } catch (RuntimeException e) {
                    TheabyssMod.LOGGER.warn("Failed to apply post effect {}", effect, e);
                    FAILED.add(effect);
                }
                restoreDepth(main);
            }
            main.bindWrite(false);
        }
        for (ResourceLocation stopped : previous) {
            clearPersistent(stopped);
        }
        depthSaved = false;
    }

    private static void restoreDepth(RenderTarget main) {
        if (depthSaved && savedDepth != null && savedDepth.width == main.width && savedDepth.height == main.height) {
            main.copyDepthFrom(savedDepth);
            main.bindWrite(false);
            mergeHand(main);
        }
    }

    private static void clearPersistent(ResourceLocation effect) {
        List<String> names = PERSISTENT.get(effect);
        PostChain chain = CHAINS.get(effect);
        if (names == null || chain == null) {
            return;
        }
        for (String name : names) {
            RenderTarget target = chain.getTempTarget(name);
            if (target != null) {
                target.clear(Minecraft.ON_OSX);
            }
        }
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
    }

    private static long size(RenderTarget target) {
        return (long)target.width << 32 | target.height & 0xFFFFFFFFL;
    }

    private static void prepare(Minecraft mc, PostChain chain) {
        RenderTarget main = mc.getMainRenderTarget();
        List<PostPass> passes = ((PostChainAccessor)chain).theabyss$passes();
        Long size = SIZES.get(chain);
        if (size == null || size != size(main)) {
            chain.resize(main.width, main.height);
            SIZES.put(chain, size(main));
        }
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 pos = camera.getPosition();
        int bx = Mth.floor(pos.x);
        int by = Mth.floor(pos.y);
        int bz = Mth.floor(pos.z);
        float ox = (float)(bx - pos.x);
        float oy = (float)(by - pos.y);
        float oz = (float)(bz - pos.z);
        float gameTime = RenderSystem.getShaderGameTime();
        float depthFar = mc.gameRenderer.getDepthFar();
        for (PostPass pass : passes) {
            EffectInstance effect = pass.getEffect();
            effect.safeGetUniform("GameTime").set(gameTime);
            effect.safeGetUniform("CameraBlockPos").set(bx, by, bz);
            effect.safeGetUniform("CameraOffset").set(ox, oy, oz);
            effect.safeGetUniform("DepthFar").set(depthFar);
        }
    }
}
