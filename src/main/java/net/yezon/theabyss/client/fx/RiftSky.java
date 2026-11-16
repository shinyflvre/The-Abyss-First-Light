package net.yezon.theabyss.client.fx;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.Optional;
import java.util.OptionalDouble;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;
import net.neoforged.neoforge.client.event.RegisterCustomEnvironmentEffectRendererEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.yezon.theabyss.TheabyssMod;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class RiftSky {
    public static final Identifier SKYBOX = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "riftofsins");

    static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "pipeline/rift_sky"))
        .withVertexShader(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "core/rift_sky"))
        .withFragmentShader(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "core/rift_sky"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .withColorTargetState(ColorTargetState.DEFAULT)
        .withCull(false)
        .build();

    private RiftSky() {
    }

    @SubscribeEvent
    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(PIPELINE);
    }

    @SubscribeEvent
    public static void registerSkybox(RegisterCustomEnvironmentEffectRendererEvent event) {
        event.registerSkyboxRenderer(SKYBOX, new Renderer());
    }

    private static final class Renderer implements CustomSkyboxRenderer {
        private @Nullable GpuBuffer cube;

        @Override
        public boolean renderSky(LevelRenderState levelRenderState, SkyRenderState sky, Matrix4fc modelViewMatrix, GpuBufferSlice skyFog) {
            if (this.cube == null) {
                this.cube = buildCube();
            }
            RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
            RenderSystem.AutoStorageIndexBuffer quadIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
            RenderSystem.setShaderFog(skyFog);
            try (RenderPass pass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(() -> "Rift of Sins sky", target.getColorTextureView(), Optional.empty(), target.getDepthTextureView(), OptionalDouble.empty())) {
                pass.setPipeline(RenderSystem.getCompiledPipeline(PIPELINE));
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("DynamicTransforms", RenderSystem.getDynamicUniforms()
                    .writeTransform(new Matrix4f(modelViewMatrix), new Vector4f(1.0F), new Vector3f(), new Matrix4f()));
                pass.setVertexBuffer(0, this.cube.slice());
                pass.setIndexBuffer(quadIndices.getBuffer(36), quadIndices.type());
                pass.drawIndexed(36, 1, 0, 0, 0);
            }
            return true;
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
                    return RenderSystem.getDevice().createBuffer(() -> "Rift of Sins sky", GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
                }
            }
        }
    }
}
