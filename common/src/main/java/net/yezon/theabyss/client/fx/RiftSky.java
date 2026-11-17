package net.yezon.theabyss.client.fx;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.client.event.RegisterDimensionSpecialEffectsEvent;
import net.yezon.theabyss.platform.client.event.RegisterShadersEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class RiftSky {
    public static final ResourceLocation SKYBOX = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "riftofsins");
    private static final ResourceLocation SHADER_ID = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "rift_sky");
    private static @Nullable ShaderInstance shader;

    private RiftSky() {
    }

    public static void lightmapSky(org.joml.Vector3f sky) {
        net.minecraft.client.multiplayer.ClientLevel level = net.minecraft.client.Minecraft.getInstance().level;
        if (level != null && level.effects() instanceof Effects) {
            sky.set(0x8C / 255.0F, 0x8C / 255.0F, 0x94 / 255.0F);
        }
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        event.registerShader(SHADER_ID, DefaultVertexFormat.POSITION, loaded -> shader = loaded);
    }

    @SubscribeEvent
    public static void registerSkybox(RegisterDimensionSpecialEffectsEvent event) {
        event.register(SKYBOX, new Effects());
    }

    public static final class Effects extends DimensionSpecialEffects implements CustomSky {
        private @Nullable VertexBuffer cube;

        public Effects() {
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
            ShaderInstance program = shader;
            if (program == null || isFoggy) {
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
            if (this.cube == null) {
                this.cube = buildCube();
            }
            RenderSystem.depthMask(false);
            RenderSystem.disableBlend();
            RenderSystem.disableCull();
            this.cube.bind();
            this.cube.drawWithShader(modelViewMatrix, projectionMatrix, program);
            VertexBuffer.unbind();
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            return true;
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
            VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            buffer.bind();
            buffer.upload(builder.buildOrThrow());
            VertexBuffer.unbind();
            return buffer;
        }
    }
}
