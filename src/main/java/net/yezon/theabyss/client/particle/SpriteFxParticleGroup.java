package net.yezon.theabyss.client.particle;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;

public class SpriteFxParticleGroup extends ParticleGroup<SpriteFxParticle> {
    public static final ParticleRenderType TYPE = new ParticleRenderType("THEABYSS_SPRITE_FX", "TAFX");
    private final QuadParticleRenderState renderState = new QuadParticleRenderState();

    public SpriteFxParticleGroup(ParticleEngine engine) {
        super(engine);
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
        for (SpriteFxParticle particle : this.particles) {
            if (frustum.isVisible(particle.cullBox())) {
                try {
                    particle.extract(this.renderState, camera, partialTickTime);
                } catch (Throwable throwable) {
                    CrashReport report = CrashReport.forThrowable(throwable, "Rendering Particle");
                    CrashReportCategory category = report.addCategory("Particle being rendered");
                    category.setDetail("Particle", particle::toString);
                    throw new ReportedException(report);
                }
            }
        }
        return this.renderState;
    }
}
