package net.yezon.theabyss.events;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import org.jspecify.annotations.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.client.fx.CustomSky;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.RegisterDimensionSpecialEffectsEvent;
import net.yezon.theabyss.platform.client.event.RegisterShadersEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

@EventBusSubscriber(value = Dist.CLIENT)
public class RenderSkyEvents {
    public static final String SKYBOX = "theabyss:the_abyss";
    private static final ResourceKey<Level> THE_ABYSS = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("theabyss:the_abyss"));
    private static final ResourceLocation MOON = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
    private static final int DEEP_SKY_OVERLAY = 0xE106141F;
    private static @Nullable ShaderInstance skyShader;
    private static @Nullable ShaderInstance nebulaShader;

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
    public static void onFogColor(net.yezon.theabyss.platform.client.event.ViewportEvent.ComputeFogColor event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level.dimension() != THE_ABYSS || !nebulaSky()) {
            return;
        }
        event.setRed(event.getRed() * 0.62F);
        event.setGreen(event.getGreen() * 0.62F);
        event.setBlue(event.getBlue() * 0.62F);
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        event.registerShader(ResourceLocation.fromNamespaceAndPath("theabyss", "abyss_sky"), DefaultVertexFormat.POSITION, shader -> skyShader = shader);
        event.registerShader(ResourceLocation.fromNamespaceAndPath("theabyss", "abyss_nebula"), DefaultVertexFormat.POSITION, shader -> nebulaShader = shader);
    }

    @SubscribeEvent
    public static void registerSkybox(RegisterDimensionSpecialEffectsEvent event) {
        event.register(ResourceLocation.parse(SKYBOX), new AbyssSkyEffects());
    }

    public static void execute(LevelAccessor world) {
    }

    public static final class AbyssSkyEffects extends DimensionSpecialEffects implements CustomSky {
        private @Nullable VertexBuffer topDisc;
        private @Nullable VertexBuffer bottomDisc;
        private @Nullable VertexBuffer stars;
        private @Nullable VertexBuffer nebulaCube;

        public AbyssSkyEffects() {
            super(Float.NaN, true, DimensionSpecialEffects.SkyType.NORMAL, false, true);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
            return fogColor;
        }

        @Override
        public boolean isFoggyAt(int x, int z) {
            return false;
        }

        @Override
        public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
            ShaderInstance sky = skyShader;
            ShaderInstance nebula = nebulaShader;
            if (sky == null || nebula == null || isFoggy) {
                return false;
            }
            FogType fogType = camera.getFluidInCamera();
            if (fogType == FogType.POWDER_SNOW || fogType == FogType.LAVA) {
                return false;
            }
            if (camera.getEntity() instanceof LivingEntity living && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS))) {
                return false;
            }
            setupFog.run();
            FogRenderer.levelFogColor();
            if (this.topDisc == null) {
                this.topDisc = buildDisc(16.0F, 512.0F);
                this.bottomDisc = buildDisc(-16.0F, -512.0F);
                this.stars = buildStars();
                this.nebulaCube = buildCube();
            }
            RenderSystem.depthMask(false);
            if (nebulaSky()) {
                this.renderNebula(level, partialTick, nebula, modelViewMatrix, projectionMatrix);
                RenderSystem.depthMask(true);
                return true;
            }
            Minecraft minecraft = Minecraft.getInstance();
            float timeOfDayAngle = level.getTimeOfDay(partialTick) * 360.0F;
            float rainBrightness = 1.0F - level.getRainLevel(partialTick);
            sky.safeGetUniform("SkyRenderDistance").set(minecraft.gameRenderer.getRenderDistance());

            Vec3 skyColor = level.getSkyColor(camera.getPosition(), partialTick);
            RenderSystem.disableBlend();
            RenderSystem.disableCull();
            RenderSystem.setShaderColor((int)((float)skyColor.x * 255.0F) / 255.0F, (int)((float)skyColor.y * 255.0F) / 255.0F,
                (int)((float)skyColor.z * 255.0F) / 255.0F, 1.0F);
            draw(this.topDisc, new Matrix4f(modelViewMatrix), projectionMatrix, sky);
            RenderSystem.enableCull();

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
            Matrix4f moonView = new Matrix4f(modelViewMatrix).rotate(Axis.ZP.rotationDegrees(timeOfDayAngle));
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, rainBrightness);
            this.drawMoon(level.getMoonPhase(), moonView);

            float starColor = (int)(level.getStarBrightness(partialTick) * 255.0F) / 255.0F;
            Matrix4f starView = new Matrix4f(modelViewMatrix).rotate(Axis.YN.rotationDegrees(90.0F)).rotate(Axis.XP.rotationDegrees(timeOfDayAngle));
            RenderSystem.disableCull();
            RenderSystem.setShaderColor(starColor, starColor, starColor, starColor * rainBrightness);
            FogRenderer.setupNoFog();
            draw(this.stars, starView, projectionMatrix, GameRenderer.getPositionShader());
            setupFog.run();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();

            LocalPlayer player = minecraft.player;
            if (player != null && player.getEyePosition(partialTick).y() - level.getLevelData().getHorizonHeight(level) < 0.0) {
                RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
                draw(this.bottomDisc, new Matrix4f(modelViewMatrix).translate(0.0F, 12.0F, 0.0F), projectionMatrix, sky);
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor((DEEP_SKY_OVERLAY >> 16 & 0xFF) / 255.0F, (DEEP_SKY_OVERLAY >> 8 & 0xFF) / 255.0F,
                (DEEP_SKY_OVERLAY & 0xFF) / 255.0F, (DEEP_SKY_OVERLAY >>> 24) / 255.0F);
            draw(this.topDisc, new Matrix4f(modelViewMatrix), projectionMatrix, sky);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.depthMask(true);
            return true;
        }

        private void renderNebula(ClientLevel level, float partialTick, ShaderInstance nebula, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
            float angle = level.getSunAngle(partialTick);
            nebula.safeGetUniform("Eclipse").set(Mth.sin(angle), -Mth.cos(angle), 0.0F, 1.0F - level.getRainLevel(partialTick));
            nebula.safeGetUniform("SkyFlags").set(net.yezon.theabyss.weather.client.AbyssLightningClient.flash(), pixelSky() ? 1.0F : 0.0F);
            RenderSystem.disableBlend();
            RenderSystem.disableCull();
            draw(this.nebulaCube, new Matrix4f(modelViewMatrix), projectionMatrix, nebula);
            RenderSystem.enableCull();
        }

        private static void draw(@Nullable VertexBuffer buffer, Matrix4f modelView, Matrix4f projection, @Nullable ShaderInstance shader) {
            if (buffer == null || shader == null) {
                return;
            }
            buffer.bind();
            buffer.drawWithShader(modelView, projection, shader);
            VertexBuffer.unbind();
        }

        private void drawMoon(int phase, Matrix4f moonView) {
            int column = phase % 4;
            int row = phase / 4 % 2;
            float u0 = column / 4.0F;
            float v0 = row / 2.0F;
            float u1 = (column + 1) / 4.0F;
            float v1 = (row + 1) / 2.0F;
            float r = 20.0F;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, MOON);
            Matrix4fStack stack = RenderSystem.getModelViewStack();
            stack.pushMatrix();
            stack.identity();
            RenderSystem.applyModelViewMatrix();
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.addVertex(moonView, -r, -100.0F, -r).setUv(u1, v1);
            builder.addVertex(moonView, -r, -100.0F, r).setUv(u0, v1);
            builder.addVertex(moonView, r, -100.0F, r).setUv(u0, v0);
            builder.addVertex(moonView, r, -100.0F, -r).setUv(u1, v0);
            com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(builder.buildOrThrow());
            stack.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }

        private static VertexBuffer upload(MeshData mesh) {
            VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            buffer.bind();
            buffer.upload(mesh);
            VertexBuffer.unbind();
            return buffer;
        }

        private static VertexBuffer buildCube() {
            float s = 100.0F;
            float[][] faces = {
                {-s, -s, -s, s, -s, -s, s, -s, s, -s, -s, s},
                {-s, s, -s, -s, s, s, s, s, s, s, s, -s},
                {-s, -s, -s, -s, s, -s, s, s, -s, s, -s, -s},
                {-s, -s, s, s, -s, s, s, s, s, -s, s, s},
                {-s, -s, -s, -s, -s, s, -s, s, s, -s, s, -s},
                {s, -s, -s, s, s, -s, s, s, s, s, -s, s},
            };
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            for (float[] face : faces) {
                for (int v = 0; v < 4; v++) {
                    builder.addVertex(face[v * 3], face[v * 3 + 1], face[v * 3 + 2]);
                }
            }
            return upload(builder.buildOrThrow());
        }

        private static VertexBuffer buildDisc(float y, float xRadius) {
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
            builder.addVertex(0.0F, y, 0.0F);
            for (int i = 0; i <= 8; i++) {
                float angle = i * 45.0F * Mth.DEG_TO_RAD;
                builder.addVertex(xRadius * Mth.cos(angle), y, 512.0F * Mth.sin(angle));
            }
            return upload(builder.buildOrThrow());
        }

        private static VertexBuffer buildStars() {
            RandomSource random = RandomSource.create(10842L);
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
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
                    float roll = (float)random.nextDouble() * (float)Math.PI * 2.0F;
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
            return upload(builder.buildOrThrow());
        }
    }
}
