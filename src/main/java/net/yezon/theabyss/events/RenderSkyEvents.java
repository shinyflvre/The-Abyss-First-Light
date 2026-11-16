package net.yezon.theabyss.events;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.Optional;
import java.util.OptionalDouble;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.MoonPhase;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterCustomEnvironmentEffectRendererEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

@EventBusSubscriber(value = Dist.CLIENT)
public class RenderSkyEvents {
    public static final String SKYBOX = "theabyss:the_abyss";
    private static final ResourceKey<Level> THE_ABYSS = ResourceKey.create(Registries.DIMENSION, Identifier.parse("theabyss:the_abyss"));
    private static final ContextKey<Boolean> BELOW_HORIZON = new ContextKey<>(Identifier.fromNamespaceAndPath("theabyss", "sky_below_horizon"));
    private static final int DEEP_SKY_OVERLAY = 0xE106141F;

    static final RenderPipeline SKY_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("theabyss", "pipeline/abyss_sky"))
        .withVertexShader(Identifier.fromNamespaceAndPath("theabyss", "core/abyss_sky"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("theabyss", "core/abyss_sky"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION)
        .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_FAN)
        .withColorTargetState(ColorTargetState.DEFAULT)
        .withCull(false)
        .build();
    static final RenderPipeline SKY_OVERLAY_PIPELINE = SKY_PIPELINE.toBuilder()
        .withLocation(Identifier.fromNamespaceAndPath("theabyss", "pipeline/abyss_sky_overlay"))
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .build();
    static final RenderPipeline STARS_PIPELINE = RenderPipelines.STARS.toBuilder()
        .withLocation(Identifier.fromNamespaceAndPath("theabyss", "pipeline/abyss_stars"))
        .withCull(false)
        .build();

    static final RenderPipeline NEBULA_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("theabyss", "pipeline/abyss_nebula"))
        .withVertexShader(Identifier.fromNamespaceAndPath("theabyss", "core/abyss_nebula"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("theabyss", "core/abyss_nebula"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .withColorTargetState(ColorTargetState.DEFAULT)
        .withCull(false)
        .build();

    private static boolean pixelSky() {
        try {
            return net.yezon.theabyss.configuration.ConfigConfiguration.MINECRAFT_LIKE_SKY.get();
        } catch (IllegalStateException notLoadedYet) {
            return false;
        }
    }

    static boolean nebulaSky() {
        try {
            return net.yezon.theabyss.configuration.ConfigConfiguration.ABYSS_NEBULA_SKY.get();
        } catch (IllegalStateException notLoadedYet) {
            return true;
        }
    }

    @SubscribeEvent
    public static void onFogColor(net.neoforged.neoforge.client.event.ViewportEvent.ComputeFogColor event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level.dimension() != THE_ABYSS || !nebulaSky()) {
            return;
        }
        event.setRed(event.getRed() * 0.62F);
        event.setGreen(event.getGreen() * 0.62F);
        event.setBlue(event.getBlue() * 0.62F);
    }

    @SubscribeEvent
    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(NEBULA_PIPELINE);
        event.registerPipeline(SKY_PIPELINE);
        event.registerPipeline(SKY_OVERLAY_PIPELINE);
        event.registerPipeline(STARS_PIPELINE);
    }

    @SubscribeEvent
    public static void registerSkybox(RegisterCustomEnvironmentEffectRendererEvent event) {
        event.registerSkyboxRenderer(Identifier.parse(SKYBOX), new AbyssSkyRenderer());
    }

    @SubscribeEvent
    public static void extractSky(ExtractLevelRenderStateEvent event) {
        execute(event, event.getLevel());
    }

    public static void execute(LevelAccessor world) {
        execute(null, world);
    }

    private static void execute(@Nullable ExtractLevelRenderStateEvent event, LevelAccessor world) {
        if (event == null) {
            return;
        }

        if ((world instanceof Level _lvl ? _lvl.dimension() : Level.OVERWORLD) == THE_ABYSS) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            ClientLevel level = event.getLevel();
            float partialTick = event.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            boolean belowHorizon = player != null && player.getEyePosition(partialTick).y() - level.getLevelData().getHorizonHeight(level) < 0.0;
            event.getRenderState().setRenderData(BELOW_HORIZON, belowHorizon);
        }
    }

    private static final class AbyssSkyRenderer implements CustomSkyboxRenderer {
        private static final int DISC_VERTICES = 10;
        private @Nullable GpuBuffer topDisc;
        private @Nullable GpuBuffer bottomDisc;
        private @Nullable GpuBuffer stars;
        private int starIndexCount;
        private @Nullable GpuBuffer moonPhases;
        private @Nullable TextureAtlasSprite moonPhasesSprite;
        private @Nullable GpuBuffer nebulaCube;

        @Override
        public boolean renderSky(LevelRenderState levelRenderState, SkyRenderState sky, Matrix4fc modelViewMatrix, GpuBufferSlice skyFog) {
            Minecraft minecraft = Minecraft.getInstance();
            TextureAtlas celestials = minecraft.getAtlasManager().getAtlasOrThrow(AtlasIds.CELESTIALS);
            if (this.topDisc == null || levelRenderState.shouldResetSkyRenderer || celestials.getSprite(moonSprite(MoonPhase.FULL_MOON)) != this.moonPhasesSprite) {
                this.rebuildBuffers(celestials);
            }

            RenderTarget target = minecraft.gameRenderer.mainRenderTarget();
            RenderSystem.AutoStorageIndexBuffer quadIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
            if (nebulaSky()) {
                this.renderNebula(target, quadIndices, sky, modelViewMatrix, skyFog);
                return true;
            }
            float timeOfDayAngle = sky.sunAngle * Mth.RAD_TO_DEG;
            float rainBrightness = sky.rainBrightness;
            RenderSystem.setShaderFog(skyFog);

            try (RenderPass pass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(() -> "The Abyss sky", target.getColorTextureView(), Optional.empty(), target.getDepthTextureView(), OptionalDouble.empty())) {
                Vector4f skyColor = new Vector4f(
                    (int)(sky.skyColor.x() * 255.0F) / 255.0F, (int)(sky.skyColor.y() * 255.0F) / 255.0F, (int)(sky.skyColor.z() * 255.0F) / 255.0F, 1.0F
                );
                this.drawDisc(pass, SKY_PIPELINE, this.topDisc, new Matrix4f(modelViewMatrix), skyColor, "Abyss sky disc");

                Matrix4f moonView = new Matrix4f(modelViewMatrix).rotate(Axis.ZP.rotationDegrees(timeOfDayAngle));
                pass.pushDebugGroup(() -> "Abyss moon");
                pass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.CELESTIAL));
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("DynamicTransforms", RenderSystem.getDynamicUniforms().writeTransform(moonView, new Vector4f(1.0F, 1.0F, 1.0F, rainBrightness)));
                pass.setUniform("Sampler0", celestials.getTextureView(), celestials.getSampler());
                pass.setVertexBuffer(0, this.moonPhases.slice());
                pass.setIndexBuffer(quadIndices.getBuffer(6), quadIndices.type());
                pass.drawIndexed(6, 1, 0, sky.moonPhase.index() * 4, 0);
                pass.popDebugGroup();

                float starColor = (int)(sky.starBrightness * 255.0F) / 255.0F;
                Matrix4f starView = new Matrix4f(modelViewMatrix).rotate(Axis.YN.rotationDegrees(90.0F)).rotate(Axis.XP.rotationDegrees(timeOfDayAngle));
                pass.pushDebugGroup(() -> "Abyss stars");
                pass.setPipeline(RenderSystem.getCompiledPipeline(STARS_PIPELINE));
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform(
                    "DynamicTransforms",
                    RenderSystem.getDynamicUniforms().writeTransform(starView, new Vector4f(starColor, starColor, starColor, starColor * rainBrightness))
                );
                pass.setVertexBuffer(0, this.stars.slice());
                pass.setIndexBuffer(quadIndices.getBuffer(this.starIndexCount), quadIndices.type());
                pass.drawIndexed(this.starIndexCount, 1, 0, 0, 0);
                pass.popDebugGroup();

                if (Boolean.TRUE.equals(levelRenderState.getRenderData(BELOW_HORIZON))) {
                    Matrix4f abyssView = new Matrix4f(modelViewMatrix).translate(0.0F, 12.0F, 0.0F);
                    this.drawDisc(pass, SKY_PIPELINE, this.bottomDisc, abyssView, new Vector4f(0.0F, 0.0F, 0.0F, 1.0F), "Abyss dark disc");
                }

                Vector4f overlay = new Vector4f(
                    (DEEP_SKY_OVERLAY >> 16 & 0xFF) / 255.0F, (DEEP_SKY_OVERLAY >> 8 & 0xFF) / 255.0F, (DEEP_SKY_OVERLAY & 0xFF) / 255.0F, (DEEP_SKY_OVERLAY >>> 24) / 255.0F
                );
                this.drawDisc(pass, SKY_OVERLAY_PIPELINE, this.topDisc, new Matrix4f(modelViewMatrix), overlay, "Abyss sky veil");
            }

            return true;
        }

        private void renderNebula(RenderTarget target, RenderSystem.AutoStorageIndexBuffer quadIndices, SkyRenderState sky, Matrix4fc modelViewMatrix,
            GpuBufferSlice skyFog) {
            if (this.nebulaCube == null) {
                this.nebulaCube = buildCube();
            }
            RenderSystem.setShaderFog(skyFog);
            float angle = sky.sunAngle;
            Vector4f eclipse = new Vector4f(Mth.sin(angle), -Mth.cos(angle), 0.0F, sky.rainBrightness);
            try (RenderPass pass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(() -> "Abyss nebula sky", target.getColorTextureView(), Optional.empty(), target.getDepthTextureView(), OptionalDouble.empty())) {
                pass.setPipeline(RenderSystem.getCompiledPipeline(NEBULA_PIPELINE));
                RenderSystem.bindDefaultUniforms(pass);
                org.joml.Vector3f flash = new org.joml.Vector3f(net.yezon.theabyss.weather.client.AbyssLightningClient.flash(), pixelSky() ? 1.0F : 0.0F, 0.0F);
                pass.setUniform("DynamicTransforms", RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(modelViewMatrix), eclipse, flash, new Matrix4f()));
                pass.setVertexBuffer(0, this.nebulaCube.slice());
                pass.setIndexBuffer(quadIndices.getBuffer(36), quadIndices.type());
                pass.drawIndexed(36, 1, 0, 0, 0);
            }
        }

        private static GpuBuffer buildCube() {
            float s = 100.0F;
            float[][] faces = {
                {-s, -s, -s, s, -s, -s, s, -s, s, -s, -s, s},
                {-s, s, -s, -s, s, s, s, s, s, s, s, -s},
                {-s, -s, -s, -s, s, -s, s, s, -s, s, -s, -s},
                {-s, -s, s, s, -s, s, s, s, s, -s, s, s},
                {-s, -s, -s, -s, -s, s, -s, s, s, -s, s, -s},
                {s, -s, -s, s, s, -s, s, s, s, s, -s, s},
            };
            try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(24 * DefaultVertexFormat.POSITION.getVertexSize())) {
                BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION);
                for (float[] face : faces) {
                    for (int v = 0; v < 4; v++) {
                        builder.addVertex(face[v * 3], face[v * 3 + 1], face[v * 3 + 2]);
                    }
                }
                try (MeshData mesh = builder.buildOrThrow()) {
                    return RenderSystem.getDevice().createBuffer(() -> "Abyss nebula sky", GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
                }
            }
        }

        private void drawDisc(RenderPass pass, RenderPipeline pipeline, GpuBuffer disc, Matrix4f modelView, Vector4f color, String label) {
            pass.pushDebugGroup(() -> label);
            pass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", RenderSystem.getDynamicUniforms().writeTransform(modelView, color));
            pass.setVertexBuffer(0, disc.slice());
            pass.draw(DISC_VERTICES, 1, 0, 0);
            pass.popDebugGroup();
        }

        private static Identifier moonSprite(MoonPhase phase) {
            return Identifier.withDefaultNamespace("moon/" + phase.getSerializedName());
        }

        private void rebuildBuffers(TextureAtlas celestials) {
            this.close();
            this.topDisc = buildDisc("Abyss sky disc", 16.0F, 512.0F);
            this.bottomDisc = buildDisc("Abyss dark disc", -16.0F, -512.0F);
            this.stars = this.buildStars();
            this.moonPhases = buildMoonPhases(celestials);
            this.moonPhasesSprite = celestials.getSprite(moonSprite(MoonPhase.FULL_MOON));
        }

        private void close() {
            for (GpuBuffer buffer : new GpuBuffer[] {this.topDisc, this.bottomDisc, this.stars, this.moonPhases}) {
                if (buffer != null) {
                    buffer.close();
                }
            }
        }

        private static GpuBuffer buildDisc(String label, float y, float xRadius) {
            try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(DISC_VERTICES * DefaultVertexFormat.POSITION.getVertexSize())) {
                BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
                builder.addVertex(0.0F, y, 0.0F);

                for (int i = 0; i <= 8; i++) {
                    float angle = i * 45.0F * Mth.DEG_TO_RAD;
                    builder.addVertex(xRadius * Mth.cos(angle), y, 512.0F * Mth.sin(angle));
                }

                try (MeshData mesh = builder.buildOrThrow()) {
                    return RenderSystem.getDevice().createBuffer(() -> label, GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
                }
            }
        }

        private GpuBuffer buildStars() {
            RandomSource random = RandomSource.create(10842L);

            try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION.getVertexSize() * 1500 * 4)) {
                BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION);

                for (int i = 0; i < 1500; i++) {
                    float x = random.nextFloat() * 2.0F - 1.0F;
                    float y = random.nextFloat() * 2.0F - 1.0F;
                    float z = random.nextFloat() * 2.0F - 1.0F;
                    float size = 0.15F + 0.1F * random.nextFloat();
                    float lengthSq = x * x + y * y + z * z;
                    if (lengthSq < 1.0F && lengthSq > 0.01F) {
                        float scale = 1.0F / Mth.sqrt(lengthSq);
                        x *= scale;
                        y *= scale;
                        z *= scale;
                        float px = x * 100.0F;
                        float py = y * 100.0F;
                        float pz = z * 100.0F;
                        float yaw = (float)Math.atan2(x, z);
                        float sinYaw = Mth.sin(yaw);
                        float cosYaw = Mth.cos(yaw);
                        float pitch = (float)Math.atan2(Mth.sqrt(x * x + z * z), y);
                        float sinPitch = Mth.sin(pitch);
                        float cosPitch = Mth.cos(pitch);
                        float roll = (float)random.nextDouble() * (float) Math.PI * 2.0F;
                        float sinRoll = Mth.sin(roll);
                        float cosRoll = Mth.cos(roll);

                        for (int j = 0; j < 4; j++) {
                            float a = ((j & 2) - 1) * size;
                            float b = ((j + 1 & 2) - 1) * size;
                            float u = a * cosRoll - b * sinRoll;
                            float v = b * cosRoll + a * sinRoll;
                            float w = -u * cosPitch;
                            builder.addVertex(px + (w * sinYaw - v * cosYaw), py + u * sinPitch, pz + (v * sinYaw + w * cosYaw));
                        }
                    }
                }

                try (MeshData mesh = builder.buildOrThrow()) {
                    this.starIndexCount = mesh.drawState().indexCount();
                    return RenderSystem.getDevice().createBuffer(() -> "Abyss stars", GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
                }
            }
        }

        private static GpuBuffer buildMoonPhases(TextureAtlas atlas) {
            MoonPhase[] phases = MoonPhase.values();
            float r = 20.0F;

            try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(phases.length * 4 * DefaultVertexFormat.POSITION_TEX.getVertexSize())) {
                BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX);

                for (MoonPhase phase : phases) {
                    TextureAtlasSprite sprite = atlas.getSprite(moonSprite(phase));
                    builder.addVertex(-r, -100.0F, -r).setUv(sprite.getU1(), sprite.getV1());
                    builder.addVertex(-r, -100.0F, r).setUv(sprite.getU0(), sprite.getV1());
                    builder.addVertex(r, -100.0F, r).setUv(sprite.getU0(), sprite.getV0());
                    builder.addVertex(r, -100.0F, -r).setUv(sprite.getU1(), sprite.getV0());
                }

                try (MeshData mesh = builder.buildOrThrow()) {
                    return RenderSystem.getDevice().createBuffer(() -> "Abyss moon phases", GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
                }
            }
        }
    }
}
