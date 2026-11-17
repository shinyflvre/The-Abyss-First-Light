package net.yezon.theabyss.fabric.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Map;
import java.util.Queue;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.yezon.theabyss.client.particle.AbyssParticleRenderTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineCustomTypesMixin {
    @Shadow
    @Final
    private Map<ParticleRenderType, Queue<Particle>> particles;

    @Shadow
    @Final
    private TextureManager textureManager;

    @Inject(method = "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;depthMask(Z)V"))
    private void theabyss$renderCustomTypes(LightTexture lightTexture, Camera camera, float partialTick, CallbackInfo ci) {
        boolean drawn = false;
        for (ParticleRenderType type : AbyssParticleRenderTypes.ALL) {
            Queue<Particle> queue = this.particles.get(type);
            if (queue == null || queue.isEmpty()) {
                continue;
            }
            RenderSystem.setShader(GameRenderer::getParticleShader);
            BufferBuilder builder = type.begin(Tesselator.getInstance(), this.textureManager);
            if (builder == null) {
                continue;
            }
            drawn = true;
            for (Particle particle : queue) {
                try {
                    particle.render(builder, camera, partialTick);
                } catch (Throwable throwable) {
                    CrashReport report = CrashReport.forThrowable(throwable, "Rendering Particle");
                    CrashReportCategory category = report.addCategory("Particle being rendered");
                    category.setDetail("Particle", particle::toString);
                    category.setDetail("Particle Type", type::toString);
                    throw new ReportedException(report);
                }
            }
            MeshData mesh = builder.build();
            if (mesh != null) {
                BufferUploader.drawWithShader(mesh);
            }
        }
        if (drawn) {
            RenderSystem.defaultBlendFunc();
        }
    }
}
