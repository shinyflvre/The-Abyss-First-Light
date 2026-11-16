package net.yezon.theabyss.mixin.accessor.client;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.Map;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPipelines.class)
public interface RenderPipelinesAccessor {
    @Accessor("MATRICES_FOG_SNIPPET")
    static RenderPipeline.Snippet theabyss$matricesFogSnippet() {
        throw new AssertionError();
    }

    @Accessor("PARTICLE_SNIPPET")
    static RenderPipeline.Snippet theabyss$particleSnippet() {
        throw new AssertionError();
    }

    @Accessor("ENERGY_SWIRL_SNIPPET")
    static RenderPipeline.Snippet theabyss$energySwirlSnippet() {
        throw new AssertionError();
    }

    @Accessor("GUI_TEXTURED_SNIPPET")
    static RenderPipeline.Snippet theabyss$guiTexturedSnippet() {
        throw new AssertionError();
    }

    @Accessor("PIPELINES_BY_LOCATION")
    static Map<Identifier, RenderPipeline> theabyss$pipelinesByLocation() {
        throw new AssertionError();
    }
}
